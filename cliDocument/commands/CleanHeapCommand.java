package com.commands;
import com.utils.MinHeap;
import com.utils.Command;

/**
 * Command implementation for clearing all elements from the collection.
 * This command removes all music bands from the collection's heap data structure.
 */
public class CleanHeapCommand implements Command{
    /** Reference to the singleton MinHeap instance */
    MinHeap heap = MinHeap.getInstance();
    
    /**
     * Executes the clean heap command, removing all elements.
     */
    @Override
    public void execute() {
        heap.clear();
        System.out.println("All elements deleted");
    }
}
