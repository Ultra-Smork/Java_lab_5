package com.server.network;

import com.common.*;
import com.server.LoggingMiddleware;
import com.utils.MinHeap;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Non-blocking TCP server using Java NIO (New I/O).
 * 
 * This server accepts client connections and processes requests
 * using asynchronous I/O operations. Unlike traditional blocking
 * servers, this can handle many concurrent connections without
 * creating a thread for each client.
 * 
 * Key features:
 * - Uses AsynchronousServerSocketChannel for non-blocking accept
 * - Uses AsynchronousSocketChannel for non-blocking read/write
 * - Maintains a set of all connected clients
 * - Provides server statistics via getStats()
 * - Uses ForkJoinPool for multi-threaded request reading
 * - Uses Thread for request processing
 * - Uses CachedThreadPool for response sending
 * 
 * Communication flow:
 * 1. Server starts and listens on specified port
 * 2. Client connects via AsynchronousSocketChannel
 * 3. Server accepts connection and starts reading
 * 4. Server reads request (using ForkJoinPool), processes it (new Thread), sends response (CachedThreadPool)
 * 5. Server waits for next request or client disconnect
 */
public class AsyncServer {
    /** Port number to listen on */
    private final int port;
    
    /** Flag to control server running state */
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    /** When the server started (used for calculating uptime) */
    private final LocalDateTime startTime = LocalDateTime.now();
    
    /** Set of all connected clients - uses ConcurrentHashMap for thread safety */
    private final Set<AsynchronousSocketChannel> clients = ConcurrentHashMap.newKeySet();
    
    /** The server channel that accepts incoming connections */
    private AsynchronousServerSocketChannel serverChannel;
    
    /** Handler that processes requests and generates responses */
    private final RequestHandler requestHandler;
    
    /** CachedThreadPool for multi-threaded response sending */
    private final ExecutorService responseExecutor;

    /**
     * Creates a new server on the specified port.
     * 
     * @param port The port number to listen on (e.g., 8080)
     */
    public AsyncServer(int port) {
        this.port = port;
        this.requestHandler = new RequestHandler();
        this.responseExecutor = Executors.newCachedThreadPool();
    }

    /**
     * Starts the server and begins accepting clients.
     * This is a non-blocking operation - the method returns immediately
     * while the server continues running in background.
     * 
     * @throws IOException If the server cannot bind to the port
     */
    public void start() throws IOException {
        // Open a non-blocking server socket channel
        serverChannel = AsynchronousServerSocketChannel.open();
        
        // Bind to the specified port
        serverChannel.bind(new InetSocketAddress(port));
        
        // Mark server as running
        running.set(true);

        System.out.println("Server started on port " + port);
        
        // Start accepting clients (runs in background)
        acceptClients();
    }

    /**
     * Continuously accepts incoming client connections.
     * This runs in a loop as long as the server is running.
     * Each new client is passed to handleClient() for processing.
     */
    private void acceptClients() {
        // Register an accept callback
        // When a client connects, the completed() method is called
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel clientChannel, Void attachment) {
                // If server is still running, accept more clients
                if (running.get()) {
                    // Accept the next client (this creates the recursive loop)
                    serverChannel.accept(null, this);
                    // Handle this client
                    handleClient(clientChannel);
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                // Log error and continue accepting if server is still running
                if (running.get()) {
                    System.err.println("Failed to accept client: " + exc.getMessage());
                    serverChannel.accept(null, this);
                }
            }
        });
    }

    /**
     * Handles communication with a single client.
     * This includes reading requests, processing them, and sending responses.
     * 
     * @param clientChannel The channel for communicating with this client
     */
    private void handleClient(AsynchronousSocketChannel clientChannel) {
        // Get client info for logging
        String clientInfo = getClientInfo(clientChannel);
        
        // Add client to connected set
        clients.add(clientChannel);
        System.out.println("Client connected. Active connections: " + clients.size());
        
        // Log client connection
        LoggingMiddleware.logClientConnected(clientInfo);

        // Start reading with a fresh buffer for this client
        readRequest(clientChannel, clientInfo);
    }
    
    private void readRequest(AsynchronousSocketChannel clientChannel, String clientInfo) {
        // Create a fresh buffer for each read operation to avoid thread issues
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        
        // Use native async read (AsynchronousSocketChannel handles internally)
        try {
            clientChannel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer bytesRead, ByteBuffer attachment) {
                    // Check if client disconnected
                    if (bytesRead <= 0) {
                        closeClient(clientChannel, clientInfo);
                        return;
                    }
                    
                    // Prepare buffer for reading
                    attachment.flip();
                    
                    // Convert buffer bytes to byte array
                    byte[] data = new byte[attachment.remaining()];
                    attachment.get(data);

                    try {
                        // Deserialize the request
                        Request request = Serializer.deserialize(data);
                        
                        // Process request in a new Thread
                        Thread processingThread = new Thread(() -> {
                            try {
                                Response response = requestHandler.handle(request, clientInfo, clientChannel);
                                sendResponse(clientChannel, response, clientInfo);
                            } catch (Exception e) {
                                System.err.println("Error handling request: " + e.getMessage());
                                sendErrorResponse(clientChannel, Response.error("Server error: " + e.getMessage()), clientInfo);
                            }
                        });
                        processingThread.start();
                        
                    } catch (Exception e) {
                        // Handle errors during request processing
                        System.err.println("Error handling request: " + e.getMessage());
                        LoggingMiddleware.logError(clientInfo, "Error handling request: " + e.getMessage(), e);
                        // Send error response to client
                        Response errorResp = Response.error("Server error: " + e.getMessage());
                        sendErrorResponse(clientChannel, errorResp, clientInfo);
                    }
                }
                
                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    // Handle closed channel or other errors gracefully
                    if (exc instanceof java.nio.channels.ClosedChannelException) {
                        closeClient(clientChannel, clientInfo);
                    } else {
                        System.err.println("Error reading request: " + exc.getMessage());
                        closeClient(clientChannel, clientInfo);
                    }
                }
            });
        } catch (Exception e) {
            // Handle case where channel is already closed
            System.err.println("Error starting read: " + e.getMessage());
            closeClient(clientChannel, clientInfo);
        }
    }
    
    private void sendResponse(AsynchronousSocketChannel clientChannel, Response response, String clientInfo) {
        // Use CachedThreadPool for sending response
        responseExecutor.submit(() -> {
            try {
                // Serialize the response
                byte[] responseData = Serializer.serialize(response);
                
                // Send response back to client
                ByteBuffer responseBuffer = ByteBuffer.wrap(responseData);
                clientChannel.write(responseBuffer, responseBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                    @Override
                    public void completed(Integer written, ByteBuffer buf) {
                        if (buf.hasRemaining()) {
                            clientChannel.write(buf, buf, this);
                        } else {
                            // Done writing, wait for next request from this client
                            // Create fresh buffer for next read
                            readRequest(clientChannel, clientInfo);
                        }
                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer buf) {
                        // Handle closed channel gracefully
                        if (exc instanceof java.nio.channels.ClosedChannelException) {
                            closeClient(clientChannel, clientInfo);
                        } else {
                            System.err.println("Error writing response: " + exc.getMessage());
                            closeClient(clientChannel, clientInfo);
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("Error sending response: " + e.getMessage());
                closeClient(clientChannel, clientInfo);
            }
        });
    }
    
    private void sendErrorResponse(AsynchronousSocketChannel clientChannel, Response response, String clientInfo) {
        responseExecutor.submit(() -> {
            try {
                byte[] errorData = Serializer.serialize(response);
                ByteBuffer errorBuffer = ByteBuffer.wrap(errorData);
                clientChannel.write(errorBuffer);
                closeClient(clientChannel, clientInfo);
            } catch (IOException e) {
                closeClient(clientChannel, clientInfo);
            }
        });
    }

    /**
     * Continues reading from a client after processing a request.
     * This creates a loop that keeps processing requests from the same client
     * until they disconnect using ForkJoinPool for reading.
     * 
     * @param clientChannel The channel to read from
     * @param clientInfo Client IP and port for logging
     */
    private void readNext(AsynchronousSocketChannel clientChannel, String clientInfo) {
        readRequest(clientChannel, clientInfo);
    }

    /**
     * Gets client information (IP and port) from the channel.
     * 
     * @param channel The client channel
     * @return String in format "IP:port" or "unknown" if unavailable
     */
    private String getClientInfo(AsynchronousSocketChannel channel) {
        try {
            InetSocketAddress remoteAddr = (InetSocketAddress) channel.getRemoteAddress();
            if (remoteAddr != null) {
                return remoteAddr.getAddress().getHostAddress() + ":" + remoteAddr.getPort();
            }
        } catch (IOException e) {
            // Ignore and return unknown
        }
        return "unknown";
    }

    /**
     * Closes a client connection and removes them from the active set.
     * 
     * @param clientChannel The client connection to close
     * @param clientInfo Client IP and port for logging
     */
    private void closeClient(AsynchronousSocketChannel clientChannel, String clientInfo) {
        try {
            clients.remove(clientChannel);
            clientChannel.close();
            System.out.println("Client disconnected. Active connections: " + clients.size());
            LoggingMiddleware.logClientDisconnected(clientInfo);
        } catch (IOException e) {
            System.err.println("Error closing client: " + e.getMessage());
            LoggingMiddleware.logError(clientInfo, "Error closing client: " + e.getMessage(), e);
        }
    }

    /**
     * Gets current server statistics.
     * This is called when a client sends a HEALTH request.
     * 
     * @return ServerStats containing current server state
     */
    public ServerStats getStats() {
        // Get JVM memory info
        Runtime runtime = Runtime.getRuntime();
        
        // Calculate uptime in milliseconds
        long uptime = System.currentTimeMillis() - startTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        // Create and return stats object
        return new ServerStats(
            uptime,
            runtime.freeMemory(),
            runtime.totalMemory(),
            clients.size(),
            MinHeap.getInstance().getElementCount(),
            startTime
        );
    }

    /**
     * Stops the server and disconnects all clients.
     * 
     * @throws IOException If there's an error closing the server
     */
    public void stop() throws IOException {
        running.set(false);
        
        // Close all client connections
        for (AsynchronousSocketChannel client : clients) {
            try {
                client.close();
            } catch (IOException e) {
                // Ignore errors when closing
            }
        }
        clients.clear();
        
        // Close the server channel
        if (serverChannel != null) {
            serverChannel.close();
        }
        System.out.println("Server stopped.");
    }

    /**
     * Checks if the server is currently running.
     * 
     * @return true if server is running, false otherwise
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Gets the port number the server is listening on.
     * 
     * @return The port number
     */
    public int getPort() {
        return port;
    }

    public void broadcastNotification(String message, AsynchronousSocketChannel excludeClient) {
        for (AsynchronousSocketChannel client : clients) {
            if (client != excludeClient && client.isOpen()) {
                try {
                    Response notification = Response.notification(message);
                    byte[] data = Serializer.serialize(notification);
                    ByteBuffer buffer = ByteBuffer.wrap(data);
                    client.write(buffer, null, new CompletionHandler<Integer, Void>() {
                        @Override
                        public void completed(Integer written, Void attachment) {}
                        @Override
                        public void failed(Throwable exc, Void attachment) {}
                    });
                } catch (Exception e) {}
            }
        }
    }
}
