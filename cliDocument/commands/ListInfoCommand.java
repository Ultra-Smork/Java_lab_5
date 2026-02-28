package com.commands;
import com.utils.MinHeap;
import com.utils.Command;

/**
 * Command implementation for displaying collection information.
 * This command shows metadata about the collection including type,
 * initialization date, element count, and data file path.
 */
public class ListInfoCommand implements Command{
    /** Reference to the singleton MinHeap instance */
    MinHeap heap = MinHeap.getInstance();
    
    /**
     * Executes the list info command, displaying collection metadata.
     */
    @Override
    public void execute() {
            heap.printMetadata();
    }
}
