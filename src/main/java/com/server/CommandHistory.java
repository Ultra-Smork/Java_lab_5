package com.server;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks command history on the server side.
 * Stores the last 11 commands that were executed on the server,
 * regardless of whether they came from scripts, clients, or CLI.
 * 
 * This is separate from MinHeap's metadata history which tracks
 * collection operations (insert, update, delete).
 */
public class CommandHistory {
    /** Maximum number of commands to store */
    private static final int MAX_HISTORY = 11;
    
    /** List of executed commands */
    private final List<String> history;

    /**
     * Creates a new CommandHistory with an empty list.
     */
    public CommandHistory() {
        this.history = new ArrayList<>();
    }

    /**
     * Adds a command to the history.
     * If history exceeds MAX_HISTORY, removes the oldest command.
     * 
     * @param command The command string to add
     */
    public synchronized void addCommand(String command) {
        history.add(command);
        if (history.size() > MAX_HISTORY) {
            history.remove(0);
        }
    }

    /**
     * Gets all commands in history as a formatted string.
     * Commands are joined with newlines.
     * 
     * @return Formatted string of all commands
     */
    public synchronized String getHistoryString() {
        if (history.isEmpty()) {
            return "No commands in history.";
        }
        return String.join("\n", history);
    }

    /**
     * Gets all commands in history as a list.
     * 
     * @return List of command strings
     */
    public synchronized List<String> getHistory() {
        return new ArrayList<>(history);
    }

    /**
     * Clears the command history.
     */
    public synchronized void clear() {
        history.clear();
    }

    /**
     * Gets the number of commands in history.
     * 
     * @return Number of commands
     */
    public synchronized int size() {
        return history.size();
    }
}
