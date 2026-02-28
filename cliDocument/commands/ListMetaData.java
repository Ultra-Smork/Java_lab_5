package com.commands;
import com.utils.MinHeap;
import com.utils.Command;

/**
 * Command implementation for listing collection metadata.
 * This command displays detailed information about the collection including
 * type, initialization date, element count, and data file path.
 */
public class ListMetaData implements Command{
    /** Reference to the singleton MinHeap instance */
    MinHeap heap = MinHeap.getInstance();
    
    /**
     * Executes the list metadata command, displaying collection information.
     */
    @Override
    public void execute() {
        heap.printMetadata();
    }
}
