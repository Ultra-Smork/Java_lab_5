package com.utils;

import com.common.Response;
import com.model.MusicBand;
import com.model.Album;
import com.model.Coordinates;
import com.model.MusicGenre;
import com.server.CommandHistory;
import com.server.DatabaseManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.utils.CollectionFileManager;

/**
 * Registry of all available commands on the server.
 *
 * This class holds a map of command names to command implementations.
 * When a command request comes in, this registry executes the
 * appropriate command and returns the result.
 *
 * Uses Java Stream API for processing collections:
 * - Filtering commands by name
 * - Mapping results to strings
 * - Collecting results
 *
 * Commands supported:
 * - show: Display all music bands
 * - info: Display collection info
 * - help: Display available commands
 * - clear: Clear the collection
 * - save: Save collection to file
 * - history: Display command history
 * - average_of_number_of_participants: Calculate average participants
 * - add: Add a new music band
 * - add_if_min: Add if ID is less than minimum
 * - update: Update an existing band
 * - remove_by_id: Remove by ID
 * - remove_greater: Remove greater than ID
 * - remove_any_by_best_album: Remove by album name
 * - count_by_number_of_participants: Count by participants
 * - participants_by_id: Show participants for specific ID
 * - execute_script: Execute commands from file
 */
public class CommandRegistry {
    /** Map of command names to command implementations */
    private final Map<String, Command> commands;

    /** The invoker (used for some commands) */
    private final Invoker invoker;

    /** Command history tracker (optional, for history command) */
    private CommandHistory commandHistory;

    /**
     * Creates a new CommandRegistry and registers all available commands.
     * With command history for tracking executed commands.
     *
     * @param commandHistory The CommandHistory instance to use for history command
     */
    public CommandRegistry(CommandHistory commandHistory) {
        this.commands = new HashMap<>();
        this.invoker = new Invoker();
        this.commandHistory = commandHistory;
        registerCommands();
    }

    /**
     * Registers all available commands in the map.
     */
    private void registerCommands() {
        // Command: show - displays all music bands sorted by name
        commands.put("show", args -> {
            MinHeap heap = MinHeap.getInstance();
            List<String> result = heap.getAllElements().stream()
                .sorted(Comparator.comparing(MusicBand::getName, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(MusicBand::toString)
                .collect(Collectors.toList());
            return Response.success(String.join("\n", result));
        });

        // Command: info - displays collection information
        commands.put("info", args -> {
            int count = DatabaseManager.getBandCount();
            String info = String.format("Type: MusicBand (PostgreSQL)\nInitialization date: %s\nElements: %d",
                LocalDateTime.now().toString(),
                count);
            return Response.success(info);
        });

        // Command: help - displays available commands
        commands.put("help", args -> {
            String help = commands.keySet().stream()
                .sorted()
                .collect(Collectors.joining(", "));
            return Response.success(help);
        });

        // Command: clear - clears the collection
        commands.put("clear", args -> {
            MinHeap.getInstance().clear();
            return Response.success("Collection cleared");
        });

        // Note: save command is disabled for clients - server saves automatically
        // This is handled silently by MinHeap on collection changes

        // Command: history - displays command history (from server) or metadata history (fallback)
        commands.put("history", args -> {
            // If we have command history (from server), use it
            if (commandHistory != null) {
                return Response.success(commandHistory.getHistoryString());
            }
            // Otherwise fall back to metadata history
            MinHeap heap = MinHeap.getInstance();
            return Response.success(heap.getMetadataHistory().stream()
                .collect(Collectors.joining("\n")));
        });

        // Command: average_of_number_of_participants
        commands.put("average_of_number_of_participants", args -> {
            MinHeap heap = MinHeap.getInstance();
            double avg = heap.getAllElements().stream()
                .mapToInt(MusicBand::getNumberOfParticipants)
                .average()
                .orElse(0.0);
            return Response.success(String.format("%.2f", avg));
        });

        // Command: add - adds a new music band
        commands.put("add", args -> {
            if (args == null || args.get("band") == null) {
                return Response.error("Missing band data for add command");
            }
            MusicBand band = (MusicBand) args.get("band");
            MinHeap.getInstance().insert(band);
            return Response.success("Added new band:\n" + band);
        });

        // Command: add_if_min - adds if ID is less than minimum
        commands.put("add_if_min", args -> {
            if (args == null || args.get("band") == null) {
                return Response.error("Missing band data for add_if_min command");
            }
            MusicBand band = (MusicBand) args.get("band");
            MinHeap heap = MinHeap.getInstance();
            MusicBand currentMin = heap.peek();
            if (currentMin != null && band.getId() >= currentMin.getId()) {
                return Response.error("Element not added: ID must be less than current minimum (" + currentMin.getId() + ")");
            }
            heap.insert(band);
            return Response.success("Added new band (ID was less than minimum):\n" + band);
        });

        // Command: update - updates an existing band
        commands.put("update", args -> {
            MinHeap heap = MinHeap.getInstance();

            // If no band data provided, return existing band for update
            if (args == null || args.get("band") == null) {
                if (args == null || args.get("id") == null) {
                    return Response.error("Missing ID for update command");
                }
                Long id = ((Number) args.get("id")).longValue();
                MusicBand existing = heap.findById(id);
                if (existing == null) {
                    return Response.error("No MusicBand found with id " + id);
                }
                return Response.withData(existing);
            }

            // If band data provided, update the band
            MusicBand band = (MusicBand) args.get("band");
            if (args.get("id") != null) {
                Long id = ((Number) args.get("id")).longValue();
                band.setId(id);
            }
            heap.updateElement(band);
            return Response.success("MusicBand updated successfully!\n" + band);
        });

        // Command: remove_by_id
        commands.put("remove_by_id", args -> {
            if (args == null || args.get("id") == null) {
                return Response.error("Missing ID for remove_by_id command");
            }
            Long id = ((Number) args.get("id")).longValue();
            MinHeap heap = MinHeap.getInstance();
            boolean removed = heap.removeElById(id);
            if (removed) {
                return Response.success("MusicBand with id " + id + " has been removed.");
            } else {
                return Response.error("No MusicBand found with id " + id);
            }
        });

        // Command: remove_greater
        commands.put("remove_greater", args -> {
            if (args == null || args.get("id") == null) {
                return Response.error("Missing ID for remove_greater command");
            }
            Long id = ((Number) args.get("id")).longValue();
            MinHeap heap = MinHeap.getInstance();
            int count = heap.removeElementsGreaterThanId(id);
            return Response.success("Removed " + count + " elements with ID greater than " + id);
        });

        // Command: remove_any_by_best_album
        commands.put("remove_any_by_best_album", args -> {
            if (args == null || args.get("album") == null) {
                return Response.error("Missing album name for remove_any_by_best_album command");
            }
            String albumName = (String) args.get("album");
            MinHeap heap = MinHeap.getInstance();
            boolean removed = heap.removeElByBestAlbum(albumName);
            if (removed) {
                return Response.success("Removed one MusicBand with best album: " + albumName);
            } else {
                return Response.error("No MusicBand found with best album: " + albumName);
            }
        });

        // Command: count_by_number_of_participants
        commands.put("count_by_number_of_participants", args -> {
            if (args == null || args.get("count") == null) {
                return Response.error("Missing count for count_by_number_of_participants command");
            }
            int count = ((Number) args.get("count")).intValue();
            MinHeap heap = MinHeap.getInstance();
            int result = heap.countByNumberOfParticipants(count);
            return Response.success("Number of bands with " + count + " participants: " + result);
        });

        // Command: participants_by_id
        commands.put("participants_by_id", args -> {
            if (args == null || args.get("id") == null) {
                return Response.error("Missing ID for participants_by_id command");
            }
            Long id = ((Number) args.get("id")).longValue();
            MinHeap heap = MinHeap.getInstance();
            MusicBand band = heap.findById(id);
            if (band == null) {
                return Response.error("No MusicBand found with id " + id);
            }
            return Response.success("Band: " + band.getName() + ", Participants: " + band.getNumberOfParticipants());
        });

        // Command: execute_script - executes commands from a file
        commands.put("execute_script", args -> {
            if (args == null || args.get("path") == null) {
                return Response.error("Missing file path for execute_script command");
            }
            String filePath = (String) args.get("path");
            String resolvedPath = CollectionFileManager.resolvePath(filePath);

            if (resolvedPath == null) {
                return Response.error("Invalid file path: " + filePath);
            }

            // Try to read and execute the script
            try (BufferedReader reader = new BufferedReader(new FileReader(resolvedPath))) {
                StringBuilder results = new StringBuilder();
                String line;
                int lineNum = 0;

                while ((line = reader.readLine()) != null) {
                    lineNum++;
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }

                    // Execute each command
                    Response cmdResult = executeCommand(trimmed);
                    results.append("Line ").append(lineNum).append(": ").append(cmdResult.getResult() != null ? cmdResult.getResult() : cmdResult.getError()).append("\n");
                }

                return Response.success("Script executed. Results:\n" + results.toString());
            } catch (IOException e) {
                return Response.error("Script file not found");
            }
        });
    }

    /**
     * Executes a single command by parsing the command string.
     * Used by execute_script to run commands from a file.
     *
     * @param commandLine The command line to execute
     * @return Response from the command
     */
    private Response executeCommand(String commandLine) {
        String[] parts = commandLine.trim().split("\\s+");
        String cmd = parts[0].toLowerCase();

        // Handle commands with arguments
        switch (cmd) {
            case "remove_by_id":
                if (parts.length > 1) {
                    try {
                        Long id = Long.parseLong(parts[1]);
                        Map<String, Object> args = new HashMap<>();
                        args.put("id", id);
                        return execute("remove_by_id", args);
                    } catch (NumberFormatException e) {
                        return Response.error("Invalid ID: " + parts[1]);
                    }
                }
                return Response.error("Missing ID");

            case "remove_greater":
                if (parts.length > 1) {
                    try {
                        Long id = Long.parseLong(parts[1]);
                        Map<String, Object> args = new HashMap<>();
                        args.put("id", id);
                        return execute("remove_greater", args);
                    } catch (NumberFormatException e) {
                        return Response.error("Invalid ID: " + parts[1]);
                    }
                }
                return Response.error("Missing ID");

            case "remove_any_by_best_album":
                if (parts.length > 1) {
                    String album = commandLine.substring(commandLine.indexOf(" ") + 1);
                    Map<String, Object> args = new HashMap<>();
                    args.put("album", album);
                    return execute("remove_any_by_best_album", args);
                }
                return Response.error("Missing album name");

            case "count_by_number_of_participants":
                if (parts.length > 1) {
                    try {
                        Integer count = Integer.parseInt(parts[1]);
                        Map<String, Object> args = new HashMap<>();
                        args.put("count", count);
                        return execute("count_by_number_of_participants", args);
                    } catch (NumberFormatException e) {
                        return Response.error("Invalid count: " + parts[1]);
                    }
                }
                return Response.error("Missing count");

            case "participants_by_id":
                if (parts.length > 1) {
                    try {
                        Long id = Long.parseLong(parts[1]);
                        Map<String, Object> args = new HashMap<>();
                        args.put("id", id);
                        return execute("participants_by_id", args);
                    } catch (NumberFormatException e) {
                        return Response.error("Invalid ID: " + parts[1]);
                    }
                }
                return Response.error("Missing ID");

            case "add":
            case "add_if_min":
            case "update":
            case "clear":
            case "save":
            case "show":
            case "info":
            case "help":
            case "history":
            case "average_of_number_of_participants":
                // These commands require interactive input or complex arguments
                // For script execution, they need special handling
                return execute(cmd, null);

            default:
                return Response.error("Unknown command: " + cmd);
        }
    }

    /**
     * Executes a command by name with arguments from a Map.
     * Uses Stream API to find the matching command.
     *
     * @param commandName The name of the command
     * @param args Optional arguments for the command
     * @return Response from the command execution
     */
    public Response execute(String commandName, Map<String, Object> args) {
        return commands.entrySet().stream()
            .filter(entry -> entry.getKey().equalsIgnoreCase(commandName))
            .findFirst()
            .map(entry -> entry.getValue().execute(args))
            .orElse(Response.error("Unknown command: " + commandName));
    }

    /**
     * Executes a command with a data object (for add, add_if_min, update).
     *
     * @param commandName The name of the command
     * @param args Optional arguments (like id)
     * @param data The data object (like MusicBand)
     * @return Response from the command execution
     */
    public Response executeWithData(String commandName, Map<String, Object> args, Object data) {
        if (args == null) {
            args = new HashMap<>();
        }
        if (data != null) {
            args.put("band", data);
        }
        return execute(commandName, args);
    }

    /**
     * Gets a list of all available command names.
     *
     * @return Sorted list of command names
     */
    public List<String> getCommandNames() {
        return commands.keySet().stream()
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * Functional interface for commands.
     * Takes optional arguments and returns a Response.
     */
    @FunctionalInterface
    private interface Command {
        Response execute(Map<String, Object> args);
    }
}
