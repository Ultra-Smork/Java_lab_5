package com.common;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Contains server statistics sent back to clients in response to HEALTH requests.
 * This class is Serializable so it can be sent over TCP.
 * 
 * Provides information about:
 * - Server uptime (how long server has been running)
 * - Memory usage (free and total)
 * - Number of active client connections
 * - Size of the music band collection
 * - Server start time
 */
public class ServerStats implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** How long server has been running in milliseconds */
    private long uptimeMillis;
    
    /** Amount of free memory in bytes (from JVM) */
    private long freeMemory;
    
    /** Total amount of memory in bytes (from JVM) */
    private long totalMemory;
    
    /** Amount of used memory in bytes (total - free) */
    private long usedMemory;
    
    /** Number of currently connected clients */
    private int activeConnections;
    
    /** Number of music bands in the collection */
    private int collectionSize;
    
    /** When the server started */
    private LocalDateTime startTime;
    
    /** Whether the server is healthy (true if running) */
    private boolean isHealthy;

    /**
     * Default constructor required for deserialization.
     */
    public ServerStats() {}

    /**
     * Constructor with all statistics.
     * 
     * @param uptimeMillis How long server has been running in ms
     * @param freeMemory Free memory in bytes
     * @param totalMemory Total memory in bytes
     * @param activeConnections Number of connected clients
     * @param collectionSize Number of music bands
     * @param startTime When server started
     */
    public ServerStats(long uptimeMillis, long freeMemory, long totalMemory, 
                       int activeConnections, int collectionSize, LocalDateTime startTime) {
        this.uptimeMillis = uptimeMillis;
        this.freeMemory = freeMemory;
        this.totalMemory = totalMemory;
        this.usedMemory = totalMemory - freeMemory;
        this.activeConnections = activeConnections;
        this.collectionSize = collectionSize;
        this.startTime = startTime;
        this.isHealthy = true;
    }

    /**
     * Formats uptime as human-readable string.
     * Example outputs: "1d 2h 3m 4s", "2h 30m 15s", "45s"
     * 
     * @return Formatted uptime string
     */
    public String getUptimeFormatted() {
        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours % 24, minutes % 60, seconds % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    /**
     * Formats memory as human-readable string.
     * Example output: "256MB / 512MB"
     * 
     * @return Formatted memory string
     */
    public String getMemoryFormatted() {
        return String.format("%dMB / %dMB", usedMemory / (1024 * 1024), totalMemory / (1024 * 1024));
    }

    // Getters and setters below

    public long getUptimeMillis() { return uptimeMillis; }
    public void setUptimeMillis(long uptimeMillis) { this.uptimeMillis = uptimeMillis; }

    public long getFreeMemory() { return freeMemory; }
    public void setFreeMemory(long freeMemory) { this.freeMemory = freeMemory; }

    public long getTotalMemory() { return totalMemory; }
    public void setTotalMemory(long totalMemory) { this.totalMemory = totalMemory; }

    public long getUsedMemory() { return usedMemory; }
    public void setUsedMemory(long usedMemory) { this.usedMemory = usedMemory; }

    public int getActiveConnections() { return activeConnections; }
    public void setActiveConnections(int activeConnections) { this.activeConnections = activeConnections; }

    public int getCollectionSize() { return collectionSize; }
    public void setCollectionSize(int collectionSize) { this.collectionSize = collectionSize; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public boolean isHealthy() { return isHealthy; }
    public void setHealthy(boolean healthy) { isHealthy = healthy; }
}
