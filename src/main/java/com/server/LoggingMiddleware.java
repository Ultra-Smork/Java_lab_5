package com.server;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logging middleware for server commands.
 * Logs every command execution with client info, command details, duration, and result.
 * Similar to HTTP request logging.
 */
public class LoggingMiddleware {
    
    /** Logger instance for server commands */
    private static final Logger logger = Logger.getLogger("ServerCommands");
    
    /**
     * Logs a command execution with all details.
     * 
     * @param clientInfo Client IP and port (e.g., "/127.0.0.1:54321")
     * @param command The command name
     * @param args Command arguments (can be null)
     * @param startTime Start time in milliseconds
     * @param success Whether the command succeeded
     * @param resultOrError Success message or error description
     */
    public static void logCommand(String clientInfo, String command, 
                                  Map<String, Object> args, long startTime, 
                                  boolean success, String resultOrError) {
        long duration = System.currentTimeMillis() - startTime;
        Level level = success ? Level.INFO : Level.WARNING;
        
        StringBuilder msg = new StringBuilder();
        msg.append("Command executed | ");
        msg.append("Client: ").append(clientInfo).append(" | ");
        msg.append("Command: ").append(command).append(" | ");
        
        if (args != null && !args.isEmpty()) {
            msg.append("Args: ").append(sanitizeArgs(args)).append(" | ");
        }
        
        msg.append("Duration: ").append(duration).append("ms | ");
        msg.append("Result: ");
        
        if (success) {
            msg.append("SUCCESS");
        } else {
            msg.append("ERROR - ").append(resultOrError);
        }
        
        logger.log(level, msg.toString());
    }
    
    /**
     * Logs a command execution without arguments.
     * 
     * @param clientInfo Client IP and port
     * @param command The command name
     * @param startTime Start time in milliseconds
     * @param success Whether the command succeeded
     * @param resultOrError Success message or error description
     */
    public static void logCommand(String clientInfo, String command,
                                  long startTime, boolean success, String resultOrError) {
        logCommand(clientInfo, command, null, startTime, success, resultOrError);
    }
    
    /**
     * Logs a command execution with data object.
     * 
     * @param clientInfo Client IP and port
     * @param command The command name
     * @param args Command arguments
     * @param hasData Whether the command had a data payload
     * @param startTime Start time in milliseconds
     * @param success Whether the command succeeded
     * @param resultOrError Success message or error description
     */
    public static void logCommand(String clientInfo, String command,
                                  Map<String, Object> args, boolean hasData,
                                  long startTime, boolean success, String resultOrError) {
        long duration = System.currentTimeMillis() - startTime;
        Level level = success ? Level.INFO : Level.WARNING;
        
        StringBuilder msg = new StringBuilder();
        msg.append("Command executed | ");
        msg.append("Client: ").append(clientInfo).append(" | ");
        msg.append("Command: ").append(command).append(" | ");
        
        if (args != null && !args.isEmpty()) {
            msg.append("Args: ").append(sanitizeArgs(args)).append(" | ");
        }
        
        if (hasData) {
            msg.append("Data: [MusicBand object] | ");
        }
        
        msg.append("Duration: ").append(duration).append("ms | ");
        msg.append("Result: ");
        
        if (success) {
            msg.append("SUCCESS");
        } else {
            msg.append("ERROR - ").append(resultOrError);
        }
        
        logger.log(level, msg.toString());
    }
    
    /**
     * Sanitizes arguments for logging - removes sensitive data if any.
     * 
     * @param args The arguments to sanitize
     * @return Sanitized string representation
     */
    private static String sanitizeArgs(Map<String, Object> args) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // Don't log actual band objects - just indicate presence
            if (value instanceof com.model.MusicBand) {
                sb.append(key).append("=[MusicBand]");
            } else {
                sb.append(key).append("=").append(value);
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Logs server startup.
     * 
     * @param port The port the server is listening on
     */
    public static void logServerStart(int port) {
        logger.info("Server started on port " + port);
    }
    
    /**
     * Logs client connection.
     * 
     * @param clientInfo Client IP and port
     */
    public static void logClientConnected(String clientInfo) {
        logger.info("Client connected: " + clientInfo);
    }
    
    /**
     * Logs client disconnection.
     * 
     * @param clientInfo Client IP and port
     */
    public static void logClientDisconnected(String clientInfo) {
        logger.info("Client disconnected: " + clientInfo);
    }
    
    /**
     * Logs a server error.
     * 
     * @param clientInfo Client IP and port (can be null if no client)
     * @param error The error message
     * @param exception The exception (can be null)
     */
    public static void logError(String clientInfo, String error, Throwable exception) {
        StringBuilder msg = new StringBuilder();
        msg.append("Server error");
        if (clientInfo != null) {
            msg.append(" | Client: ").append(clientInfo);
        }
        msg.append(" | Error: ").append(error);
        
        if (exception != null) {
            logger.log(Level.SEVERE, msg.toString(), exception);
        } else {
            logger.severe(msg.toString());
        }
    }
}
