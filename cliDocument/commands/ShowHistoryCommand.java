package com.commands;
import com.utils.MinHeap;
import com.utils.Command;

/**
 * Command implementation for displaying command history.
 * This command shows the last 11 commands that were executed in the application.
 */
public class ShowHistoryCommand implements Command{
    /** Reference to the singleton MinHeap instance */
    MinHeap heap = MinHeap.getInstance();
    
    /**
     * Executes the show history command, displaying recent commands.
     */
    @Override
    public void execute() {
        heap.printHistory();
    }
}
