package com.client;

import com.client.handlers.CommandDispatcher;
import com.client.network.AsyncClient;
import com.common.Command;
import com.common.RequestBuilder;
import com.common.Response;
import com.model.MusicBand;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Entry point for the client application.
 * 
 * This class provides the command-line interface for the client.
 * It works exactly like the local CLI, but sends commands to the server
 * and receives responses back.
 * 
 * Usage:
 *   java -jar app.jar --client
 *   java -jar app.jar --client --host localhost --port 8080
 * 
 * Special commands:
 * - exit: Disconnect and quit
 * - health: Check server status
 */
public class ClientApp {
    /** Default server hostname */
    private static final String DEFAULT_HOST = "localhost";
    
    /** Default server port */
    private static final int DEFAULT_PORT = 8080;

    /**
     * Starts the client application.
     * 
     * @param host Server hostname to connect to
     * @param port Server port to connect to
     */
    public static void start(String host, int port) {
        System.out.println("Connecting to MusicBand Server at " + host + ":" + port + "...");
        
        // Create the async client
        AsyncClient client = new AsyncClient(host, port);
        
        try {
            // Connect to server
            if (!client.connect()) {
                System.err.println("Failed to connect to server");
                return;
            }
            
            System.out.println("Connected to server!");
            System.out.println("Type 'exit' to quit, 'health' to check server status\n");
            
            // Create scanner for reading user input
            Scanner scanner = new Scanner(System.in);
            
            // Main command loop
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                
                // Skip empty input
                if (input.isEmpty()) {
                    continue;
                }
                
                // Check for built-in commands
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
                
                if (input.equalsIgnoreCase("health")) {
                    printHealth(client);
                    continue;
                }
                
                // Handle the command with reconnection logic
                Response response = handleCommandWithRetry(client, input, scanner);
                
                // If response is null, reconnection failed - stay in loop
                if (response == null) {
                    // Stay in command loop, user can try again
                    continue;
                }
                
                // Display result or error
                if (response.isSuccess()) {
                    if (response.getResult() != null && !response.getResult().isEmpty()) {
                        System.out.println(response.getResult());
                    }
                    if (response.getNotification() != null && !response.getNotification().isEmpty()) {
                        System.out.println("[Notification] " + response.getNotification());
                    }
                    // Check if there's data (like for update command)
                    if (response.getData() != null) {
                        // This is an existing band for update - prompt user to modify
                        MusicBand existing = (MusicBand) response.getData();
                        MusicBand updated = com.client.handlers.ClientBandPrompt.promptForBand(scanner, existing);
                        
                        // Send the updated band back to server with retry
                        Response updateResponse = sendWithRetry(client, () -> {
                            try {
                                return client.send(
                                    RequestBuilder.command(Command.UPDATE)
                                        .withArg("id", existing.getId())
                                        .withData(updated)
                                        .withAuth()
                                        .build()
                                );
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                        
                        if (updateResponse != null && updateResponse.isSuccess()) {
                            System.out.println(updateResponse.getResult());
                        } else if (updateResponse != null) {
                            System.err.println("Error: " + updateResponse.getError());
                        }
                        // If null, reconnection failed - already handled
                    }
                } else {
                    System.err.println("Error: " + response.getError());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            // Always disconnect when done
            client.disconnect();
            System.out.println("Disconnected from server");
        }
    }

    /**
     * Handles a command with automatic reconnection on failure.
     * If server becomes unavailable, prompts user and attempts to reconnect.
     * 
     * @param client The connected client
     * @param input The user's command input
     * @param scanner Scanner for interactive prompts
     * @return Response from server, or null if reconnection failed
     */
    private static Response handleCommandWithRetry(AsyncClient client, String input, Scanner scanner) {
        try {
            return handleCommand(client, input, scanner);
        } catch (Exception e) {
            // Server might be unavailable - try to reconnect
            System.out.println("Server unavailable. Attempting to reconnect...");
            
            boolean reconnected = false;
            for (int attempt = 1; attempt <= 3; attempt++) {
                System.out.println("Reconnection attempt " + attempt + "/3...");
                try {
                    if (client.reconnect(1)) {
                        reconnected = true;
                        System.out.println("Reconnected successfully!");
                        break;
                    }
                } catch (Exception reconnectError) {
                    // Connection failed, try again
                }
                
                if (attempt < 3) {
                    System.out.println("Waiting 2 seconds before next attempt...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            if (reconnected) {
                // Try the command again
                try {
                    return handleCommand(client, input, scanner);
                } catch (Exception retryError) {
                    System.err.println("Command failed after reconnection: " + retryError.getMessage());
                    return null;
                }
            } else {
                // All reconnection attempts failed
                System.err.println("Server unavailable. Please try again later.");
                return null;
            }
        }
    }

    /**
     * Sends a request with retry logic.
     * 
     * @param client The connected client
     * @param requestSupplier Function that creates and sends the request
     * @return Response from server, or null if failed
     */
    private static Response sendWithRetry(AsyncClient client, java.util.function.Supplier<Response> requestSupplier) {
        try {
            return requestSupplier.get();
        } catch (Exception e) {
            // Try to reconnect
            System.out.println("Server unavailable. Attempting to reconnect...");
            
            boolean reconnected = false;
            for (int attempt = 1; attempt <= 3; attempt++) {
                System.out.println("Reconnection attempt " + attempt + "/3...");
                try {
                    if (client.reconnect(1)) {
                        reconnected = true;
                        System.out.println("Reconnected successfully!");
                        break;
                    }
                } catch (Exception reconnectError) {
                    // Ignore
                }
                
                if (attempt < 3) {
                    System.out.println("Waiting 2 seconds before next attempt...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            if (reconnected) {
                try {
                    return requestSupplier.get();
                } catch (Exception retryError) {
                    System.err.println("Command failed after reconnection: " + retryError.getMessage());
                    return null;
                }
            } else {
                System.err.println("Server unavailable. Please try again later.");
                return null;
            }
        }
    }

    /**
     * Handles a command from user input.
     * Parses the command and sends appropriate request to server.
     * 
     * @param client The connected client
     * @param input The user's command input
     * @param scanner Scanner for interactive prompts
     * @return Response from server
     */
    private static Response handleCommand(AsyncClient client, String input, Scanner scanner) throws Exception {
        String[] parts = input.trim().split("\\s+");
        return CommandDispatcher.dispatch(client, parts, scanner);
    }

    /**
     * Sends a health check request to the server and prints the results.
     * 
     * @param client The connected client
     */
    private static void printHealth(AsyncClient client) {
        try {
            Response response = client.send(RequestBuilder.health().build());
            
            if (response.isSuccess() && response.getStats() != null) {
                com.common.ServerStats stats = response.getStats();
                
                System.out.println("Server Status: " + (stats.isHealthy() ? "HEALTHY" : "UNHEALTHY"));
                System.out.println("Uptime: " + stats.getUptimeFormatted());
                System.out.println("Memory: " + stats.getMemoryFormatted());
                System.out.println("Active Connections: " + stats.getActiveConnections());
                System.out.println("Collection Size: " + stats.getCollectionSize());
                System.out.println("Started: " + stats.getStartTime());
            } else {
                System.err.println("Error: " + (response.getError() != null ? response.getError() : "Unknown error"));
            }
        } catch (Exception e) {
            System.err.println("Failed to get health: " + e.getMessage());
        }
    }

    /**
     * Main method - entry point for client mode.
     * 
     * @param args Command line arguments
     *             --host hostname (default: localhost)
     *             --port portnumber (default: 8080)
     */
    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        
        // Parse command line arguments
        for (int i = 0; i < args.length; i++) {
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
        
        start(host, port);
    }
}
