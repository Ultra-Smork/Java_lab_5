package com.common;

import java.io.Serializable;

/**
 * Represents a response sent from server back to client.
 * This class is Serializable so it can be converted to bytes
 * and sent over TCP using Java's object serialization.
 * 
 * Every request gets a response - either success with result data,
 * or failure with an error message.
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * Whether the request was successful.
     * True if server processed request without errors.
     */
    private boolean success;
    
    /**
     * The result of the request (if successful).
     * For COMMAND type, this contains the command output.
     * For HEALTH type, this contains "OK" string.
     */
    private String result;
    
    /**
     * Error message (if request failed).
     * Contains description of what went wrong.
     */
    private String error;
    
    /**
     * Server statistics (only used for HEALTH requests).
     * Contains uptime, memory usage, connection count, etc.
     */
    private ServerStats stats;
    
    /**
     * Data payload for commands that need to return objects.
     * Used for update command - returns existing MusicBand so client
     * can show current values and let user modify them.
     * This field is Serializable so it can be sent over the network.
     */
    private Serializable data;

    /**
     * Notification message to display to other clients.
     * This is set when another client modifies the collection.
     */
    private String notification;

    /**
     * Default constructor required for deserialization.
     * Creates an empty Response that must be populated later.
     */
    public Response() {}

    /**
     * Constructor for a successful response.
     * 
     * @param success Always true for this constructor
     * @param result The result data to send back
     */
    public Response(boolean success, String result) {
        this.success = success;
        this.result = result;
    }

    /**
     * Constructor for a response with both result and error.
     * 
     * @param success Whether the request succeeded
     * @param result The result data (can be null if error)
     * @param error Error message (if success is false)
     */
    public Response(boolean success, String result, String error) {
        this.success = success;
        this.result = result;
        this.error = error;
    }

    /**
     * Factory method to create a successful response.
     * Convenience method - equivalent to new Response(true, result)
     * 
     * @param result The result data
     * @return Response with success=true
     */
    public static Response success(String result) {
        return new Response(true, result);
    }

    /**
     * Factory method to create an error response.
     * Convenience method - equivalent to new Response(false, null, error)
     * 
     * @param error The error message
     * @return Response with success=false
     */
    public static Response error(String error) {
        return new Response(false, null, error);
    }

    /**
     * Factory method to create a health check response.
     * Creates a successful response with server statistics.
     * 
     * @param stats Server statistics to include in response
     * @return Response with stats populated
     */
    public static Response health(ServerStats stats) {
        Response resp = new Response(true, "OK");
        resp.setStats(stats);
        return resp;
    }

    /**
     * Factory method to create a response with data object.
     * Used for update command - returns existing object to client.
     * 
     * @param data The data object to include
     * @return Response with data populated
     */
    public static Response withData(Serializable data) {
        Response resp = new Response(true, null);
        resp.setData(data);
        return resp;
    }

    // Getters and setters below

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public ServerStats getStats() { return stats; }
    public void setStats(ServerStats stats) { this.stats = stats; }

    public Serializable getData() { return data; }
    public void setData(Serializable data) { this.data = data; }

    public String getNotification() { return notification; }
    public void setNotification(String notification) { this.notification = notification; }

    public static Response notification(String message) {
        Response resp = new Response(true, null);
        resp.setNotification(message);
        return resp;
    }
}
