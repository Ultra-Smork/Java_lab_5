package com.server.network;

import com.common.*;
import com.utils.MinHeap;
import com.utils.CommandRegistry;
import com.server.CommandHistory;
import com.server.LoggingMiddleware;

import java.util.*;

/**
 * Handles incoming requests and routes them to the appropriate handler.
 * 
 * This class is the bridge between the network layer (AsyncServer)
 * and the business logic (CommandRegistry). When a request comes in,
 * this class determines what type of request it is and calls the
 * appropriate handler.
 * 
 * Request types handled:
 * - COMMAND: Execute a command (show, info, help, add, update, etc.)
 * - HEALTH: Return server statistics
 * 
 * Command routing logic:
 * - Commands with no args (show, info, help, clear, save, history, average_*): execute directly
 * - Commands with args only (remove_by_id, remove_greater, etc.): get args from request
 * - Commands with data (add, add_if_min): get object from request.data
 * - Commands with both args and data (update): get id from args, object from data
 */
public class RequestHandler {
    /** Registry that holds all available commands and executes them */
    private final CommandRegistry commandRegistry;
    
    /** Command history tracker - stores last 11 commands executed on server */
    private final CommandHistory commandHistory;

    /**
     * Creates a new RequestHandler with a new CommandRegistry.
     */
    public RequestHandler() {
        this.commandHistory = new CommandHistory();
        this.commandRegistry = new CommandRegistry(commandHistory);
    }
    
    /**
     * Gets the command history tracker.
     * Used by CommandRegistry to retrieve history for the history command.
     * 
     * @return The CommandHistory instance
     */
    public CommandHistory getCommandHistory() {
        return commandHistory;
    }

    /**
     * Processes an incoming request and generates a response.
     * This is the main entry point for handling any request.
     * 
     * @param request The incoming request from client
     * @param clientInfo Client IP and port for logging
     * @return Response to send back to client
     */
    public Response handle(Request request, String clientInfo) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Check request type and route accordingly
            if (request.getType() == Request.RequestType.HEALTH) {
                Response response = handleHealth();
                LoggingMiddleware.logCommand(clientInfo, "health", startTime, response.isSuccess(), 
                    response.isSuccess() ? "OK" : response.getError());
                return response;
            }

            if (request.getType() == Request.RequestType.COMMAND) {
                Response response = handleCommand(request, clientInfo, startTime);
                return response;
            }

            // Unknown request type
            Response errorResponse = Response.error("Unknown request type");
            LoggingMiddleware.logCommand(clientInfo, "unknown", startTime, false, "Unknown request type");
            return errorResponse;
        } catch (Exception e) {
            // Catch any unexpected errors and return as error response
            LoggingMiddleware.logError(clientInfo, "Unhandled exception: " + e.getMessage(), e);
            return Response.error("Server error: " + e.getMessage());
        }
    }
    
    /**
     * Processes an incoming request without client info (for backward compatibility).
     * 
     * @param request The incoming request from client
     * @return Response to send back to client
     */
    public Response handle(Request request) {
        return handle(request, "unknown");
    }

    /**
     * Handles a health check request.
     * Gets current server statistics and returns them in a Response.
     * 
     * @return Response containing ServerStats
     */
    private Response handleHealth() {
        // Get the server instance from ServerRunner (singleton)
        AsyncServer server = ServerRunner.getServer();
        
        if (server != null) {
            // Get current stats and return as successful response
            return Response.health(server.getStats());
        }
        
        // Server not available (shouldn't happen if server is running)
        return Response.error("Server not available");
    }

    /**
     * Handles a command request.
     * Routes to CommandRegistry based on:
     * - Command name
     * - Whether args are present (for commands like remove_by_id 5)
     * - Whether data is present (for commands like add with MusicBand)
     * 
     * Also tracks the command in command history.
     * 
     * @param request The command request
     * @param clientInfo Client IP and port for logging
     * @param startTime Start time for duration calculation
     * @return Response containing command result or error
     */
    private Response handleCommand(Request request, String clientInfo, long startTime) {
        String command = request.getCommand();
        
        // Track command in history (for history command)
        // We track it before execution so even if it fails, it's still recorded
        commandHistory.addCommand(command);
        
        // Get args and data from request
        Map<String, Object> args = request.getArgs();
        Object data = request.getData();
        
        // Handle commands based on whether they need data object
        Response response;
        try {
            if (data != null) {
                // Commands that need data object: add, add_if_min, update (with new data)
                response = commandRegistry.executeWithData(command, args, data);
            } else if (args != null && !args.isEmpty()) {
                // Commands with arguments: remove_by_id, remove_greater, etc.
                response = commandRegistry.execute(command, args);
            } else {
                // Commands without args: show, info, help, save, etc.
                response = commandRegistry.execute(command, null);
            }
            
            // Log the command execution
            String resultMsg = response.isSuccess() ? 
                (response.getResult() != null ? response.getResult().substring(0, Math.min(50, response.getResult().length())) : "OK") :
                response.getError();
            
            LoggingMiddleware.logCommand(clientInfo, command, args, data != null, startTime, 
                response.isSuccess(), resultMsg);
            
            return response;
        } catch (Exception e) {
            // Log the error
            LoggingMiddleware.logCommand(clientInfo, command, args, data != null, startTime, 
                false, e.getMessage());
            return Response.error("Command execution error: " + e.getMessage());
        }
    }
}
