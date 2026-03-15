package com.client;

import com.client.network.AsyncClient;
import com.common.Request;
import com.common.Request.RequestType;
import com.common.Response;
import com.model.MusicBand;
import com.model.MusicGenre;
import com.model.Album;
import com.model.Coordinates;

import java.util.*;

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
                    // Check if there's data (like for update command)
                    if (response.getData() != null) {
                        // This is an existing band for update - prompt user to modify
                        MusicBand existing = (MusicBand) response.getData();
                        MusicBand updated = promptForBand(scanner, existing);
                        
                        // Send the updated band back to server with retry
                        Response updateResponse = sendWithRetry(client, () -> {
                            try {
                                Request updateRequest = new Request(RequestType.COMMAND, "update");
                                Map<String, Object> args = new HashMap<>();
                                args.put("id", existing.getId());
                                args.put("band", updated);
                                updateRequest.setArgs(args);
                                return client.send(updateRequest);
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
        String cmd = parts[0].toLowerCase();
        
        // Handle commands that need special processing
        switch (cmd) {
            case "add":
                return handleAdd(client, scanner);
                
            case "add_if_min":
                return handleAddIfMin(client, scanner);
                
            case "update":
                return handleUpdate(client, parts);
                
            case "execute_script":
                return handleExecuteScript(client, input);
                
            default:
                // Commands with arguments - parse them and send to server
                return handleCommandWithArgs(client, cmd, parts);
        }
    }

    /**
     * Handles commands that have arguments (like remove_by_id 5).
     */
    private static Response handleCommandWithArgs(AsyncClient client, String cmd, String[] parts) throws Exception {
        Map<String, Object> args = new HashMap<>();
        
        switch (cmd) {
            case "remove_by_id":
                if (parts.length < 2) {
                    return Response.error("Usage: remove_by_id <id>");
                }
                try {
                    args.put("id", Long.parseLong(parts[1]));
                } catch (NumberFormatException e) {
                    return Response.error("Invalid ID: " + parts[1]);
                }
                break;
                
            case "remove_greater":
                if (parts.length < 2) {
                    return Response.error("Usage: remove_greater <id>");
                }
                try {
                    args.put("id", Long.parseLong(parts[1]));
                } catch (NumberFormatException e) {
                    return Response.error("Invalid ID: " + parts[1]);
                }
                break;
                
            case "remove_any_by_best_album":
                if (parts.length < 2) {
                    return Response.error("Usage: remove_any_by_best_album <album_name>");
                }
                // Get everything after the command as the album name
                String albumName = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                args.put("album", albumName);
                break;
                
            case "count_by_number_of_participants":
                if (parts.length < 2) {
                    return Response.error("Usage: count_by_number_of_participants <count>");
                }
                try {
                    args.put("count", Integer.parseInt(parts[1]));
                } catch (NumberFormatException e) {
                    return Response.error("Invalid count: " + parts[1]);
                }
                break;
                
            case "participants_by_id":
                if (parts.length < 2) {
                    return Response.error("Usage: participants_by_id <id>");
                }
                try {
                    args.put("id", Long.parseLong(parts[1]));
                } catch (NumberFormatException e) {
                    return Response.error("Invalid ID: " + parts[1]);
                }
                break;
                
            default:
                // No arguments needed - just send the command
                break;
        }
        
        Request request = new Request(RequestType.COMMAND, cmd);
        if (!args.isEmpty()) {
            request.setArgs(args);
        }
        return client.send(request);
    }

    /**
     * Handles the add command.
     * Prompts user for all band fields locally, then sends to server.
     */
    private static Response handleAdd(AsyncClient client, Scanner scanner) throws Exception {
        MusicBand band = promptForBand(scanner, null);
        
        Request request = new Request(RequestType.COMMAND, "add");
        request.setData(band);
        
        return client.send(request);
    }

    /**
     * Handles the add_if_min command.
     * Prompts user for all band fields (including ID), then sends to server.
     */
    private static Response handleAddIfMin(AsyncClient client, Scanner scanner) throws Exception {
        // First get the ID from user with validation
        long id = 0;
        while (true) {
            System.out.print("Enter ID (must be positive and less than minimum in collection): ");
            String idStr = scanner.nextLine().trim();
            if (idStr.isEmpty()) {
                System.out.println("Error: ID cannot be empty. Please try again.");
                continue;
            }
            try {
                id = Long.parseLong(idStr);
                if (id > 0) {
                    break;
                } else {
                    System.out.println("Error: ID must be greater than 0. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid ID format. Please enter a valid number.");
            }
        }
        
        MusicBand band = promptForBand(scanner, null);
        band.setId(id);
        
        Request request = new Request(RequestType.COMMAND, "add_if_min");
        request.setData(band);
        
        return client.send(request);
    }

    /**
     * Handles the update command.
     * If ID provided, sends request to get existing band, then prompts user.
     */
    private static Response handleUpdate(AsyncClient client, String[] parts) throws Exception {
        // Check if ID is provided
        if (parts.length < 2 || !parts[1].equalsIgnoreCase("id") || parts.length < 3) {
            System.out.println("Usage: update id <id>");
            return Response.error("Usage: update id <id>");
        }
        
        long id;
        try {
            id = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            return Response.error("Invalid ID: " + parts[2] + ". Please enter a valid number.");
        }
        
        // Send request to get existing band
        Request request = new Request(RequestType.COMMAND, "update");
        Map<String, Object> args = new HashMap<>();
        args.put("id", id);
        request.setArgs(args);
        
        return client.send(request);
    }

    /**
     * Handles the execute_script command.
     * Sends file path to server for execution.
     */
    private static Response handleExecuteScript(AsyncClient client, String input) throws Exception {
        if (!input.contains(" ")) {
            return Response.error("Usage: execute_script <file_path>");
        }
        
        String filePath = input.substring(input.indexOf(" ") + 1).trim();
        
        Request request = new Request(RequestType.COMMAND, "execute_script");
        Map<String, Object> args = new HashMap<>();
        args.put("path", filePath);
        request.setArgs(args);
        
        return client.send(request);
    }

    /**
     * Prompts user for music band fields with validation.
     * If existing band provided, uses its values as defaults.
     * 
     * @param scanner Scanner for reading input
     * @param existing Existing band to use as defaults (can be null for new band)
     * @return MusicBand created from user input
     */
    private static MusicBand promptForBand(Scanner scanner, MusicBand existing) {
        MusicBand band = new MusicBand();
        
        // Name - must not be empty
        String defaultName = existing != null ? existing.getName() : "";
        String name = "";
        while (true) {
            System.out.print("Enter band name" + (existing != null ? " [" + defaultName + "]" : "") + ": ");
            name = scanner.nextLine().trim();
            if (!name.isEmpty()) {
                break;
            }
            if (existing != null && name.isEmpty()) {
                name = defaultName;
                break;
            }
            System.out.println("Error: Name cannot be empty. Please try again.");
        }
        band.setName(name);
        
        // Number of participants - must be > 0
        String defaultParticipants = existing != null ? String.valueOf(existing.getNumberOfParticipants()) : "";
        int participants = 0;
        while (true) {
            System.out.print("Enter number of members" + (existing != null ? " [" + defaultParticipants + "]" : "") + ": ");
            String participantsStr = scanner.nextLine().trim();
            if (participantsStr.isEmpty() && existing != null) {
                participants = existing.getNumberOfParticipants();
                break;
            }
            if (participantsStr.isEmpty()) {
                System.out.println("Error: Number cannot be empty. Please try again.");
                continue;
            }
            try {
                participants = Integer.parseInt(participantsStr);
                if (participants > 0) {
                    break;
                } else {
                    System.out.println("Error: Number must be greater than 0. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format. Please enter a valid integer.");
            }
        }
        band.setNumberOfParticipants(participants);
        
        // Genre - must be valid enum or empty
        String defaultGenre = existing != null && existing.getGenre() != null ? existing.getGenre().toString() : "";
        MusicGenre genre = null;
        while (true) {
            System.out.print("Enter genre (PSYCHEDELIC_ROCK, MATH_ROCK, POST_ROCK)" + (existing != null ? " [" + defaultGenre + "]" : "") + " (or press Enter to skip): ");
            String genreStr = scanner.nextLine().trim();
            if (genreStr.isEmpty()) {
                genre = existing != null ? existing.getGenre() : null;
                break;
            }
            if (genreStr.equalsIgnoreCase("null")) {
                genre = null;
                break;
            }
            try {
                genre = MusicGenre.valueOf(genreStr.toUpperCase());
                if (genre == MusicGenre.PLACEHOLDER) {
                    System.out.println("Error: PLACEHOLDER is not a valid genre. Please try again.");
                } else {
                    break;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Invalid genre. Please enter one of: PSYCHEDELIC_ROCK, MATH_ROCK, POST_ROCK or press Enter to skip.");
            }
        }
        band.setGenre(genre);
        
        // Coordinates x - must be <= 554
        long defaultX = existing != null ? existing.getCoordinates().getX() : 0;
        long x = 0;
        while (true) {
            System.out.print("Enter coordinates x (max 554)" + (existing != null ? " [" + defaultX + "]" : "") + ": ");
            String xStr = scanner.nextLine().trim();
            if (xStr.isEmpty() && existing != null) {
                x = defaultX;
                break;
            }
            if (xStr.isEmpty()) {
                System.out.println("Error: X coordinate cannot be empty. Please try again.");
                continue;
            }
            try {
                x = Long.parseLong(xStr);
                if (x <= 554) {
                    break;
                } else {
                    System.out.println("Error: X coordinate must be <= 554. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format. Please enter a valid integer.");
            }
        }
        
        // Coordinates y - must be <= 782
        int defaultY = existing != null ? existing.getCoordinates().getY() : 0;
        int y = 0;
        while (true) {
            System.out.print("Enter coordinates y (max 782)" + (existing != null ? " [" + defaultY + "]" : "") + ": ");
            String yStr = scanner.nextLine().trim();
            if (yStr.isEmpty() && existing != null) {
                y = defaultY;
                break;
            }
            if (yStr.isEmpty()) {
                System.out.println("Error: Y coordinate cannot be empty. Please try again.");
                continue;
            }
            try {
                y = Integer.parseInt(yStr);
                if (y <= 782) {
                    break;
                } else {
                    System.out.println("Error: Y coordinate must be <= 782. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format. Please enter a valid integer.");
            }
        }
        
        band.setCoordinates(new Coordinates(x, y));
        
        // Description - optional
        String defaultDesc = existing != null && existing.getDescription() != null ? existing.getDescription() : "";
        System.out.print("Enter description" + (existing != null && !defaultDesc.isEmpty() ? " [" + defaultDesc + "]" : "") + " (optional, press Enter to skip): ");
        String desc = scanner.nextLine().trim();
        band.setDescription(desc.isEmpty() ? (defaultDesc.isEmpty() ? null : defaultDesc) : desc);
        
        // Best album name - must not be empty
        String defaultAlbumName = existing != null ? existing.getBestAlbum().getName() : "";
        String albumName = "";
        while (true) {
            System.out.print("Enter best album name" + (existing != null ? " [" + defaultAlbumName + "]" : "") + ": ");
            albumName = scanner.nextLine().trim();
            if (!albumName.isEmpty()) {
                break;
            }
            if (existing != null && albumName.isEmpty()) {
                albumName = defaultAlbumName;
                break;
            }
            System.out.println("Error: Album name cannot be empty. Please try again.");
        }
        
        // Best album sales - must be > 0
        String defaultSales = existing != null ? String.valueOf(existing.getBestAlbum().getSales()) : "";
        double sales = 0;
        while (true) {
            System.out.print("Enter best album sales" + (existing != null ? " [" + defaultSales + "]" : "") + ": ");
            String salesStr = scanner.nextLine().trim();
            if (salesStr.isEmpty() && existing != null) {
                sales = existing.getBestAlbum().getSales();
                break;
            }
            if (salesStr.isEmpty()) {
                System.out.println("Error: Sales cannot be empty. Please try again.");
                continue;
            }
            try {
                sales = Double.parseDouble(salesStr);
                if (sales > 0) {
                    break;
                } else {
                    System.out.println("Error: Sales must be greater than 0. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format. Please enter a valid number.");
            }
        }
        
        band.setBestAlbum(new Album(albumName, sales));
        
        // Preserve ID and creation date for updates
        if (existing != null) {
            band.setId(existing.getId());
            band.setCreationDate(existing.getCreationDate());
        }
        
        return band;
    }

    /**
     * Sends a health check request to the server and prints the results.
     * 
     * @param client The connected client
     */
    private static void printHealth(AsyncClient client) {
        try {
            // Create health request
            Request healthRequest = new Request(Request.RequestType.HEALTH, "health");
            
            // Send and get response
            Response response = client.send(healthRequest);
            
            // Check if response is successful
            if (response.isSuccess() && response.getStats() != null) {
                com.common.ServerStats stats = response.getStats();
                
                // Print server statistics
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
