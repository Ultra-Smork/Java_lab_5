package com.server.network;

/**
 * Singleton holder for the running server instance.
 * 
 * This class provides a way for other classes (like RequestHandler)
 * to access the running server to get statistics.
 * 
 * Why do we need this?
 * - AsyncServer creates RequestHandler when it starts
 * - RequestHandler needs to call server.getStats() for health checks
 * - But RequestHandler doesn't have a reference to the server
 * - So we store the server here and RequestHandler can access it
 * 
 * Alternative approaches:
 * - Pass server reference through constructor (works but more complex)
 * - Use dependency injection framework
 * 
 * This is a simple service locator pattern.
 */
public class ServerRunner {
    /** The currently running server instance (or null if not running) */
    private static AsyncServer server;

    /**
     * Sets the server instance.
     * Called by ServerApp when starting the server.
     * 
     * @param server The running server
     */
    public static void setServer(AsyncServer server) {
        ServerRunner.server = server;
    }

    /**
     * Gets the server instance.
     * Called by RequestHandler when handling health requests.
     * 
     * @return The running server, or null if not running
     */
    public static AsyncServer getServer() {
        return server;
    }

    /**
     * Clears the server instance.
     * Called by ServerApp when stopping the server.
     */
    public static void clear() {
        server = null;
    }
}
