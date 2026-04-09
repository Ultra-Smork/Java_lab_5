package com;

import com.server.ServerApp;
import com.client.ClientApp;
import com.client.network.HealthChecker;

/**
 * Main entry point for the Music Band Collection application.
 * 
 * This class determines which mode to run in based on command line arguments:
 * 
 * 1. No arguments: Run in client mode (connects to server on localhost:8080)
 * 2. --server: Start the server (listens for client connections)
 * 3. --client: Start the client (connects to server)
 * 4. --check-health: Check if server is running and healthy
 * 
 * Examples:
 *   java -jar app.jar                    # Client mode (default)
 *   java -jar app.jar --server           # Start server on port 8080
 *   java -jar app.jar --server --port 9000 # Start server on port 9000
 *   java -jar app.jar --client           # Connect to localhost:8080
 *   java -jar app.jar --client --host server1 --port 8080
 *   java -jar app.jar --check-health     # Check server health
 */
public class App {
    /** Default port for server/client communication */
    private static final int DEFAULT_PORT = 8080;
    
    /** Default host for client to connect to */
    private static final String DEFAULT_HOST = "localhost";

    /**
     * Main entry point - determines which mode to run based on arguments.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // No arguments = run client mode (default)
        if (args.length == 0) {
            ClientApp.start(DEFAULT_HOST, DEFAULT_PORT);
            return;
        }

        // First argument determines the mode
        String mode = args[0];

        // Handle different modes using switch
        switch (mode) {
            case "--server": {
                // Server mode - start the server
                int port = DEFAULT_PORT;
                
                // Parse --port argument if present
                for (int i = 1; i < args.length; i++) {
                    if (args[i].equals("--port") && i + 1 < args.length) {
                        try {
                            port = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid port: " + args[i]);
                        }
                    }
                }
                ServerApp.start(port);
                break;
            }

            case "--client": {
                // Client mode - connect to server
                String host = DEFAULT_HOST;
                int port = DEFAULT_PORT;
                
                // Parse --host and --port arguments
                for (int i = 1; i < args.length; i++) {
                    if (args[i].equals("--host") && i + 1 < args.length) {
                        host = args[++i];
                    } else if (args[i].equals("--port") && i + 1 < args.length) {
                        try {
                            port = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid port: " + args[i]);
                        }
                    }
                }
                ClientApp.start(host, port);
                break;
            }

            case "--check-health": {
                // Health check mode - check if server is running
                String host = DEFAULT_HOST;
                int port = DEFAULT_PORT;
                
                // Parse --host and --port arguments
                for (int i = 1; i < args.length; i++) {
                    if (args[i].equals("--host") && i + 1 < args.length) {
                        host = args[++i];
                    } else if (args[i].equals("--port") && i + 1 < args.length) {
                        try {
                            port = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid port: " + args[i]);
                            System.exit(1);
                        }
                    }
                }
                // Run health checker and exit with appropriate code
                HealthChecker.main(new String[]{"--host", host, "--port", String.valueOf(port)});
                break;
            }

            default: {
                // Unknown mode - print usage information
                System.out.println("Usage:");
                System.out.println("  java -jar app.jar                    # Run in client mode (default)");
                System.out.println("  java -jar app.jar --server [--port]  # Start server");
                System.out.println("  java -jar app.jar --client [--host] [--port] # Connect to server");
                System.out.println("  java -jar app.jar --check-health [--host] [--port] # Check server health");
                System.exit(1);
            }
        }
    }
}
