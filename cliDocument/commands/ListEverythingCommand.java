package com.commands;
import com.utils.MinHeap;
import com.utils.Command;

/**
 * Command implementation for listing all elements in the collection.
 * This command displays all music bands currently stored in the collection
 * with their complete information.
 */
public class ListEverythingCommand implements Command{
    /** Reference to the singleton MinHeap instance */
    MinHeap heap = MinHeap.getInstance();
    
    /**
     * Executes the list everything command, displaying all elements.
     */
    @Override
    public void execute() {
        heap.printAll();
    }
}

