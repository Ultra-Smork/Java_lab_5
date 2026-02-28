package com.commands;
import com.utils.Command;

/**
 * Command implementation for exiting the application.
 * This command terminates the program with a status code of 0.
 */
public class ExitCommand implements Command{
    /**
     * Executes the exit command, terminating the application.
     * Displays a message and exits with status code 0.
     */
    @Override
    public void execute() {
        System.out.println("Exiting the program...");
        System.exit(0);
    }
}
