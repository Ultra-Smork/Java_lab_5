package com.client.network;

import com.common.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Non-blocking TCP client for connecting to the server.
 *
 * This class handles:
 * - Connecting to the server asynchronously
 * - Sending requests and receiving responses
 * - Managing the connection lifecycle
 * - Reconnecting when server becomes unavailable
 *
 * Uses Java NIO for non-blocking I/O, which means operations
 * like connect(), read(), and write() return immediately
 * and complete asynchronously.
 *
 * Usage:
 *   AsyncClient client = new AsyncClient("localhost", 8080);
 *   client.connect();
 *   Response response = client.send(request);
 *   client.disconnect();
 */
public class AsyncClient {
    /** Server hostname or IP address */
    private final String host;

    /** Server port number */
    private final int port;

    /** The channel for communicating with server */
    private AsynchronousSocketChannel channel;

    /** Timeout for all operations in seconds */
    private static final int TIMEOUT_SECONDS = 30;

    /** Number of retry attempts for reconnection */
    private static final int MAX_RECONNECT_ATTEMPTS = 3;

    /** Delay between reconnect attempts in milliseconds */
    private static final int RECONNECT_DELAY_MS = 500;

    /** Delay after successful reconnection before retry */
    private static final int POST_RECONNECT_DELAY_MS = 200;

    private final List<String> pendingNotifications = Collections.synchronizedList(new ArrayList<>());

    /**
     * Creates a new client that will connect to the specified server.
     *
     * @param host Server hostname or IP (e.g., "localhost" or "192.168.1.1")
     * @param port Server port number (e.g., 8080)
     */
    public AsyncClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Connects to the server.
     * This is a non-blocking operation - it returns immediately
     * and the actual connection happens in the background.
     *
     * @return true if connection succeeded
     * @throws IOException If connection fails
     */
    public boolean connect() throws IOException {
        // Close existing channel if any
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
            } catch (IOException e) {
                // Ignore
            }
        }

        // Create a new asynchronous socket channel
        channel = AsynchronousSocketChannel.open();

        // Start connecting (non-blocking)
        Future<Void> future = channel.connect(new InetSocketAddress(host, port));

        try {
            // Wait for connection to complete (with timeout)
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            // Connection failed - close the channel and throw error
            try {
                channel.close();
            } catch (IOException ex) {
                // Ignore
            }
            channel = null;
            throw new IOException("Failed to connect to " + host + ":" + port, e);
        }
    }

    /**
     * Attempts to reconnect to the server.
     * Tries to reconnect up to MAX_RECONNECT_ATTEMPTS times
     * with delays between attempts.
     *
     * @return true if reconnection succeeded
     * @throws IOException If all reconnection attempts fail
     */
    public boolean reconnect() throws IOException {
        return reconnect(MAX_RECONNECT_ATTEMPTS);
    }

    /**
     * Attempts to reconnect to the server with specified number of attempts.
     *
     * @param attempts Number of reconnection attempts
     * @return true if reconnection succeeded
     * @throws IOException If all reconnection attempts fail
     */
    public boolean reconnect(int attempts) throws IOException {
        for (int i = 1; i <= attempts; i++) {
            try {
                // Close existing connection if any
                if (channel != null && channel.isOpen()) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                }

                // Try to connect
                channel = AsynchronousSocketChannel.open();
                Future<Void> future = channel.connect(new InetSocketAddress(host, port));
                future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                return true;
            } catch (Exception e) {
                // Try again after delay
                if (i < attempts) {
                    try {
                        Thread.sleep(RECONNECT_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Sends a request to the server and waits for response.
     *
     * This method:
     * 1. Serializes the request to bytes
     * 2. Writes bytes to server
     * 3. Reads 4-byte length header from server
     * 4. Reads exactly that many bytes from server
     * 5. Deserializes response
     *
     * @param request The request to send
     * @return Response from server
     * @throws IOException If send/receive fails
     */
    public synchronized Response send(Request request) throws IOException {
        // Check if connected
        if (channel == null || !channel.isOpen()) {
            throw new IOException("Not connected to server");
        }

        // Step 1: Serialize the request to bytes
        byte[] requestData = Serializer.serialize(request);
        ByteBuffer requestBuffer = ByteBuffer.wrap(requestData);

        // Step 2: Write to server (non-blocking, but we wait for completion)
        Future<Integer> writeFuture = channel.write(requestBuffer);
        try {
            writeFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IOException("Failed to send request", e);
        }

        try {
            // Step 3: Read 4-byte length header
            ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
            readFully(channel, lengthBuffer);
            lengthBuffer.flip();
            int length = lengthBuffer.getInt();

            // Step 4: Read exactly `length` bytes
            ByteBuffer payloadBuffer = ByteBuffer.allocate(length);
            readFully(channel, payloadBuffer);
            payloadBuffer.flip();
            byte[] responseData = new byte[length];
            payloadBuffer.get(responseData);

            // Step 5: Deserialize the response
            return Serializer.deserialize(responseData);
        } catch (Exception e) {
            throw new IOException("Failed to read response", e);
        }
    }

    public Response sendOneshot(Request request) throws IOException {
        AsynchronousSocketChannel oneShotChannel = null;
        try {
            oneShotChannel = AsynchronousSocketChannel.open();
            Future<Void> connectFuture = oneShotChannel.connect(new InetSocketAddress(host, port));
            connectFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            byte[] requestData = Serializer.serialize(request);
            ByteBuffer requestBuffer = ByteBuffer.wrap(requestData);
            
            Future<Integer> writeFuture = oneShotChannel.write(requestBuffer);
            writeFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
            readFully(oneShotChannel, lengthBuffer);
            lengthBuffer.flip();
            int length = lengthBuffer.getInt();
            
            ByteBuffer payloadBuffer = ByteBuffer.allocate(length);
            readFully(oneShotChannel, payloadBuffer);
            payloadBuffer.flip();
            byte[] responseData = new byte[length];
            payloadBuffer.get(responseData);
            
            return Serializer.deserialize(responseData);
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e.getMessage(), e);
        } finally {
            if (oneShotChannel != null && oneShotChannel.isOpen()) {
                try {
                    oneShotChannel.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private static void readFully(AsynchronousSocketChannel ch, ByteBuffer dst) throws IOException {
        while (dst.hasRemaining()) {
            Future<Integer> readFuture = ch.read(dst);
            try {
                Integer bytesRead = readFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (bytesRead <= 0) {
                    throw new IOException("Server closed connection");
                }
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException("Failed to read data", e);
            }
        }
    }

    /**
     * Attempts to send a request with automatic reconnection on failure.
     * If the first attempt fails, tries to reconnect and retry once.
     *
     * @param request The request to send
     * @return Response from server
     * @throws IOException If both attempts fail
     */
    public Response sendWithRetry(Request request) throws IOException {
        try {
            return send(request);
        } catch (IOException e) {
            // Try to reconnect and retry once
            if (reconnect(1)) {
                // Small delay after reconnection to let server stabilize
                try {
                    Thread.sleep(POST_RECONNECT_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return send(request);
            }
            throw e;
        }
    }

    /**
     * Disconnects from the server.
     * Closes the channel if it's open.
     */
    public void disconnect() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            // Ignore errors during disconnect
        }
        channel = null;
    }

    /**
     * Checks if client is currently connected to server.
     *
     * @return true if connected and channel is open
     */
    public boolean isConnected() {
        if (channel == null) {
            return false;
        }
        return channel.isOpen();
    }

    /**
     * Gets the server hostname.
     *
     * @return The host
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the server port.
     *
     * @return The port
     */
    public int getPort() {
        return port;
    }

    public String pollNotification() {
        if (!pendingNotifications.isEmpty()) {
            return pendingNotifications.remove(0);
        }
        return null;
    }

    public CompletableFuture<Response> sendAsync(Request request) {
        return CompletableFuture.supplyAsync(() -> {
            AsynchronousSocketChannel oneShotChannel = null;
            try {
                oneShotChannel = AsynchronousSocketChannel.open();
                Future<Void> connectFuture = oneShotChannel.connect(new InetSocketAddress(host, port));
                connectFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                
                byte[] requestData = Serializer.serialize(request);
                ByteBuffer requestBuffer = ByteBuffer.wrap(requestData);
                
                Future<Integer> writeFuture = oneShotChannel.write(requestBuffer);
                writeFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                
                ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
                readFully(oneShotChannel, lengthBuffer);
                lengthBuffer.flip();
                int length = lengthBuffer.getInt();
                
                ByteBuffer payloadBuffer = ByteBuffer.allocate(length);
                readFully(oneShotChannel, payloadBuffer);
                payloadBuffer.flip();
                byte[] responseData = new byte[length];
                payloadBuffer.get(responseData);
                
                return Serializer.deserialize(responseData);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (oneShotChannel != null && oneShotChannel.isOpen()) {
                    try {
                        oneShotChannel.close();
                    } catch (IOException e) {
                        // ignore close error
                    }
                }
            }
        });
    }

    public CompletableFuture<Response> sendAsyncWithRetry(Request request) {
        return sendAsync(request)
            .exceptionallyCompose(e -> {
                try {
                    if (reconnect()) {
                        try {
                            Thread.sleep(POST_RECONNECT_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        return sendAsync(request);
                    }
                } catch (IOException ie2) {
                    // reconnect failed
                }
                CompletableFuture<Response> failed = new CompletableFuture<>();
                failed.completeExceptionally(new IOException(e.getMessage()));
                return failed;
            });
    }
}
