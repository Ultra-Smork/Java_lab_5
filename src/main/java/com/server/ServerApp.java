package com.server;

import com.server.network.AsyncServer;
import com.server.network.ServerRunner;

/**
 * Entry point for starting the server.
 * 
 * This class is the main entry point when running the application
 * in server mode. It:
 * 1. Initializes the MinHeap (singleton collection)
 * 2. Creates and starts the AsyncServer
 * 3. Waits for user input to stop the server
 * 
 * Usage:
 *   java -jar app.jar --server
 *   java -jar app.jar --server --port 8080
 */
public class ServerApp {
    /** Default port if none specified */
    private static final int DEFAULT_PORT = 8080;
    
    /** Reference to the running server */
    private static AsyncServer server;

    /**
     * Starts the server on the specified port.
     * 
     * @param port    The port number to listen on
     * @param jdbcUrl Optional JDBC connection string, or null for default
     */
    public static void start(int port, String jdbcUrl) {
        if (jdbcUrl != null && !jdbcUrl.isEmpty()) {
            DatabaseManager.setDatabaseUrl(jdbcUrl);
        }

        System.out.println("Starting MusicBand Server on port " + port + "...");
        System.out.println("Database: " + DatabaseManager.getDatabaseUrl());
        
        // Initialize the database
        try {
            DatabaseManager.initialize();
            System.out.println("Database initialized successfully.");
        } catch (java.sql.SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // Create the async server
        server = new AsyncServer(port);
        
        // Register the server so RequestHandler can access it for stats
        ServerRunner.setServer(server);
        
        try {
            // Start the server (non-blocking)
            server.start();
            
            // Register shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (server != null) {
                    try {
                        server.stop();
                    } catch (java.io.IOException e) {
                        // ignore
                    }
                    ServerRunner.clear();
                }
            }));
            
            // Check if stdin is available (not piped)
            if (System.in.available() > 0) {
                java.util.Scanner scanner = new java.util.Scanner(System.in);
                System.out.println("Server running. Press Enter to stop...");
                scanner.nextLine();
            } else {
                // No stdin available - just run until killed
                System.out.println("Server running. Press Ctrl+C to stop...");
                try {
                    Thread.currentThread().join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            // Stop the server
            server.stop();
            ServerRunner.clear();
        } catch (java.io.IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Main method - entry point for server mode.
     * 
     * @param args Command line arguments. First arg can be port number.
     *             Example: java ServerApp 8080
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        String jdbcUrl = null;
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--port") && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port: " + args[i]);
                }
            } else if (args[i].equals("--jdbc") && i + 1 < args.length) {
                jdbcUrl = args[++i];
            } else if (i == 0) {
                try {
                    port = Integer.parseInt(args[i]);
                } catch (NumberFormatException ignored) {}
            }
        }
        
        start(port, jdbcUrl);
    }
}
