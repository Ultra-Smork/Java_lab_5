package com.utils;

/**
 * Interface for all command implementations in the application.
 * Commands represent operations that can be executed on the collection
 * such as adding, removing, updating, or displaying elements.
 * 
 * <p>This interface provides multiple execute method variants to support
 * different command signatures:</p>
 * <ul>
 *   <li>{@link #execute()} - Basic execution without arguments</li>
 *   <li>{@link #executeWithInt(Long)} - Execution with an integer parameter</li>
 *   <li>{@link #executeWithString(String)} - Execution with a string parameter</li>
 *   <li>{@link #executeWithLong(Long)} - Execution with a long parameter</li>
 *   <li>{@link #executeWithId(Long)} - Execution with an ID parameter</li>
 * </ul>
 */
public interface Command {
    
    /**
     * Executes the command with no arguments.
     * This is the basic execution method for commands that don't require parameters.
     */
    void execute();
    
    /**
     * Executes the command with an integer parameter.
     * Default empty implementation for commands that don't need this variant.
     *
     * @param id the integer parameter for the command
     */
    default void executeWithInt(Long id) {}
    
    /**
     * Executes the command with a string parameter.
     * Default empty implementation for commands that don't need this variant.
     *
     * @param s the string parameter for the command
     */
    default void executeWithString(String s) {}
    
    /**
     * Executes the command with a long parameter.
     * Default empty implementation for commands that don't need this variant.
     *
     * @param id the long parameter for the command
     */
    default void executeWithLong(Long id) {}
    
    /**
     * Executes the command with an ID parameter.
     * Default empty implementation for commands that don't need this variant.
     *
     * @param id the ID parameter for the command
     */
    default void executeWithId(Long id) {}
}
