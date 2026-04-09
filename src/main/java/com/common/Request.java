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
    
    private RequestType type;
    
    private String command;
    
    private Command commandEnum;
    
    private Map<String, Object> args;
    
    private Serializable data;
    
    private String login;
    
    private String password;

    public Request() {}

    public Request(RequestType type, String command, Map<String, Object> args) {
        this.type = type;
        this.command = command;
        this.commandEnum = Command.fromString(command);
        this.args = args;
    }

    public Request(RequestType type, String command) {
        this.type = type;
        this.command = command;
        this.commandEnum = Command.fromString(command);
    }

    public Request(RequestType type, Command command) {
        this.type = type;
        this.command = command.name().toLowerCase();
        this.commandEnum = command;
    }

    public Request(RequestType type, Command command, Map<String, Object> args) {
        this.type = type;
        this.command = command.name().toLowerCase();
        this.commandEnum = command;
        this.args = args;
    }

    public RequestType getType() { return type; }
    public void setType(RequestType type) { this.type = type; }

    public String getCommand() { return command; }
    public void setCommand(String command) { 
        this.command = command; 
        this.commandEnum = Command.fromString(command);
    }

    public Command getCommandEnum() { return commandEnum; }
    public void setCommandEnum(Command command) { 
        this.commandEnum = command;
        this.command = command != null ? command.name().toLowerCase() : null;
    }

    public Map<String, Object> getArgs() { return args; }
    public void setArgs(Map<String, Object> args) { this.args = args; }

    public Serializable getData() { return data; }
    public void setData(Serializable data) { this.data = data; }
    
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public enum RequestType {
        COMMAND,
        HEALTH,
        FILE_UPLOAD,
        FILE_DOWNLOAD,
        EXECUTE_SQL
    }
}
