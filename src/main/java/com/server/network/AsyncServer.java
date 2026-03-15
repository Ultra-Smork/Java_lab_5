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
 * 
 * Communication flow:
 * 1. Server starts and listens on specified port
 * 2. Client connects via AsynchronousSocketChannel
 * 3. Server accepts connection and starts reading
 * 4. Server reads request, processes it, sends response
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

    /**
     * Creates a new server on the specified port.
     * 
     * @param port The port number to listen on (e.g., 8080)
     */
    public AsyncServer(int port) {
        this.port = port;
        this.requestHandler = new RequestHandler();
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

        // Create a buffer for reading client data
        ByteBuffer buffer = ByteBuffer.allocate(8192);

        // Start reading from the client (non-blocking)
        clientChannel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer bytesRead, ByteBuffer attachment) {
                // If we read some data
                if (bytesRead > 0) {
                    // Prepare buffer for reading
                    attachment.flip();
                    
                    // Convert buffer bytes to byte array
                    byte[] data = new byte[attachment.remaining()];
                    attachment.get(data);

                    try {
                        // Deserialize the request
                        Request request = Serializer.deserialize(data);
                        
                        // Process the request and get response with client info
                        Response response = requestHandler.handle(request, clientInfo);
                        
                        // Serialize the response
                        byte[] responseData = Serializer.serialize(response);
                        
                        // Send response back to client
                        ByteBuffer responseBuffer = ByteBuffer.wrap(responseData);
                        clientChannel.write(responseBuffer, responseBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                            @Override
                            public void completed(Integer written, ByteBuffer buf) {
                                // If there's more data to write, continue writing
                                if (buf.hasRemaining()) {
                                    clientChannel.write(buf, buf, this);
                                } else {
                                    // Done writing, wait for next request from this client
                                    readNext(clientChannel, attachment, clientInfo);
                                }
                            }

                            @Override
                            public void failed(Throwable exc, ByteBuffer buf) {
                                System.err.println("Error writing response: " + exc.getMessage());
                                closeClient(clientChannel, clientInfo);
                            }
                        });
                    } catch (Exception e) {
                        // Handle errors during request processing
                        System.err.println("Error handling request: " + e.getMessage());
                        LoggingMiddleware.logError(clientInfo, "Error handling request: " + e.getMessage(), e);
                        try {
                            // Send error response to client
                            Response errorResp = Response.error("Server error: " + e.getMessage());
                            byte[] errorData = Serializer.serialize(errorResp);
                            ByteBuffer errorBuffer = ByteBuffer.wrap(errorData);
                            clientChannel.write(errorBuffer);
                        } catch (IOException ex) {
                            // If we can't send error, close the connection
                            closeClient(clientChannel, clientInfo);
                        }
                    }
                } else {
                    // bytesRead <= 0 means client disconnected
                    closeClient(clientChannel, clientInfo);
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                // Client disconnected or error occurred
                System.err.println("Client disconnected or error: " + exc.getMessage());
                LoggingMiddleware.logError(clientInfo, "Client disconnected: " + exc.getMessage(), null);
                closeClient(clientChannel, clientInfo);
            }
        });
    }

    /**
     * Continues reading from a client after processing a request.
     * This creates a loop that keeps processing requests from the same client
     * until they disconnect.
     * 
     * @param clientChannel The channel to read from
     * @param buffer The buffer to use for reading
     * @param clientInfo Client IP and port for logging
     */
    private void readNext(AsynchronousSocketChannel clientChannel, ByteBuffer buffer, String clientInfo) {
        // Clear buffer and start reading again
        buffer.clear();
        clientChannel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer bytesRead, ByteBuffer attachment) {
                if (bytesRead > 0) {
                    attachment.flip();
                    byte[] data = new byte[attachment.remaining()];
                    attachment.get(data);
                    try {
                        Request request = Serializer.deserialize(data);
                        Response response = requestHandler.handle(request, clientInfo);
                        byte[] responseData = Serializer.serialize(response);
                        ByteBuffer responseBuffer = ByteBuffer.wrap(responseData);
                        clientChannel.write(responseBuffer, responseBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                            @Override
                            public void completed(Integer written, ByteBuffer buf) {
                                if (buf.hasRemaining()) {
                                    clientChannel.write(buf, buf, this);
                                } else {
                                    readNext(clientChannel, attachment, clientInfo);
                                }
                            }
                            @Override
                            public void failed(Throwable exc, ByteBuffer buf) {
                                closeClient(clientChannel, clientInfo);
                            }
                        });
                    } catch (Exception e) {
                        LoggingMiddleware.logError(clientInfo, "Error in readNext: " + e.getMessage(), e);
                        closeClient(clientChannel, clientInfo);
                    }
                } else {
                    closeClient(clientChannel, clientInfo);
                }
            }
            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                closeClient(clientChannel, clientInfo);
            }
        });
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
}
