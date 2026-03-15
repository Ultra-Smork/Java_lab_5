package com.client.network;

import com.common.Request;
import com.common.Request.RequestType;
import com.common.Response;
import com.common.ServerStats;

/**
 * Utility class for checking server health.
 * 
 * This class provides a simple way to check if the server is
 * running and healthy without starting the full client application.
 * 
 * Can be used as:
 * - A library: HealthChecker checker = new HealthChecker(host, port);
 *              boolean healthy = checker.check();
 * - A standalone program: java HealthChecker --host localhost --port 8080
 * 
 * Returns:
 * - Exit code 0 if server is healthy
 * - Exit code 1 if server is unreachable or unhealthy
 */
public class HealthChecker {
    /** Server hostname */
    private final String host;
    
    /** Server port */
    private final int port;

    /**
     * Creates a new health checker for the specified server.
     * 
     * @param host Server hostname or IP
     * @param port Server port number
     */
    public HealthChecker(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Checks if the server is healthy.
     * Connects to server, sends health request, checks response.
     * 
     * @return true if server is healthy, false otherwise
     */
    public boolean check() {
        AsyncClient client = new AsyncClient(host, port);
        try {
            // Connect to server
            if (!client.connect()) {
                return false;
            }
            
            // Send health request
            Request request = new Request(Request.RequestType.HEALTH, "health");
            Response response = client.send(request);
            
            // Check if healthy
            return response.isSuccess() && response.getStats() != null && response.getStats().isHealthy();
        } catch (Exception e) {
            // Any exception means unhealthy
            return false;
        } finally {
            client.disconnect();
        }
    }

    /**
     * Gets detailed server statistics.
     * 
     * @return ServerStats if successful, null if failed
     */
    public ServerStats getStats() {
        AsyncClient client = new AsyncClient(host, port);
        try {
            // Connect to server
            if (!client.connect()) {
                return null;
            }
            
            // Send health request
            Request request = new Request(Request.RequestType.HEALTH, "health");
            Response response = client.send(request);
            
            return response.getStats();
        } catch (Exception e) {
            return null;
        } finally {
            client.disconnect();
        }
    }

    /**
     * Main method - allows running as standalone program.
     * 
     * Usage: java HealthChecker [--host hostname] [--port port]
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;
        
        // Parse arguments
        for (int i = 0; i < args.length; i++) {
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
        
        // Create checker and get stats
        HealthChecker checker = new HealthChecker(host, port);
        ServerStats stats = checker.getStats();
        
        if (stats != null) {
            // Print server statistics
            System.out.println("Server Status: " + (stats.isHealthy() ? "HEALTHY" : "UNHEALTHY"));
            System.out.println("Uptime: " + stats.getUptimeFormatted());
            System.out.println("Memory: " + stats.getMemoryFormatted());
            System.out.println("Active Connections: " + stats.getActiveConnections());
            System.out.println("Collection Size: " + stats.getCollectionSize());
            System.out.println("Started: " + stats.getStartTime());
            System.exit(0);
        } else {
            System.err.println("Server is not responding or not healthy");
            System.exit(1);
        }
    }
}
