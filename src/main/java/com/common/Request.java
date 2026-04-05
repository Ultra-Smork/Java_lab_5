package com.common;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a request sent from client to server.
 * This class is Serializable so it can be converted to bytes
 * and sent over TCP using Java's object serialization.
 * 
 * Request types:
 * - COMMAND: execute a command (show, add, etc.)
 * - HEALTH: check server status
 * - FILE_UPLOAD: upload a file to server
 * - FILE_DOWNLOAD: download a file from server
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * The type of request - determines how server will process it.
     * Can be COMMAND, HEALTH, FILE_UPLOAD, or FILE_DOWNLOAD
     */
    private RequestType type;
    
    /**
     * The command name to execute on server.
     * Used only when type is COMMAND (e.g., "show", "info", "add")
     */
    private String command;
    
    /**
     * Additional arguments for the command.
     * Used for commands that need parameters (e.g., remove_by_id needs id)
     */
    private Map<String, Object> args;
    
    /**
     * Data payload for commands that need objects.
     * Used for sending MusicBand objects in add/add_if_min commands,
     * or for sending updated MusicBand in update command.
     * This field is Serializable so it can be sent over the network.
     */
    private Serializable data;
    
    /**
     * User's login for authentication.
     * Sent with each request to identify the user.
     */
    private String login;
    
    /**
     * User's password for authentication.
     * Sent with each request for verification.
     * Server will hash with SHA-384 and compare with stored hash.
     */
    private String password;

    /**
     * Default constructor required for deserialization.
     * Creates an empty Request that must be populated later.
     */
    public Request() {}

    /**
     * Constructor for requests with arguments.
     * 
     * @param type The type of request (COMMAND, HEALTH, etc.)
     * @param command The command name to execute
     * @param args Map of argument names to values
     */
    public Request(RequestType type, String command, Map<String, Object> args) {
        this.type = type;
        this.command = command;
        this.args = args;
    }

    /**
     * Constructor for requests without arguments.
     * 
     * @param type The type of request
     * @param command The command name to execute
     */
    public Request(RequestType type, String command) {
        this.type = type;
        this.command = command;
    }

    // Getters and setters below

    public RequestType getType() { return type; }
    public void setType(RequestType type) { this.type = type; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public Map<String, Object> getArgs() { return args; }
    public void setArgs(Map<String, Object> args) { this.args = args; }

    public Serializable getData() { return data; }
    public void setData(Serializable data) { this.data = data; }
    
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    /**
     * Enum defining all possible request types.
     * Each type represents a different category of operation.
     */
    public enum RequestType {
        /** Execute a command on the server (show, add, remove, etc.) */
        COMMAND,
        /** Check server health and get server statistics */
        HEALTH,
        /** Upload a file to the server */
        FILE_UPLOAD,
        /** Download a file from the server */
        FILE_DOWNLOAD,
        /** Execute SQL query on the database */
        EXECUTE_SQL
    }
}
