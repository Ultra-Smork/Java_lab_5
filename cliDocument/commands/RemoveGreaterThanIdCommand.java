package com.commands;
import com.utils.MinHeap;
import com.utils.Command;

/**
 * Command implementation for removing all music bands with ID greater than a specified value.
 * This command searches the collection for all bands whose ID exceeds the given threshold
 * and removes them from the collection.
 */
public class RemoveGreaterThanIdCommand implements Command {
    /** Reference to the singleton MinHeap instance */
    MinHeap heap = MinHeap.getInstance();
    
    /**
     * Executes the command - does nothing (use executeWithLong instead).
     */
    @Override
    public void execute() {}
    
    /**
     * Removes all music bands with ID greater than the specified value.
     *
     * @param id the ID threshold - all bands with ID greater than this will be removed
     */
    @Override
    public void executeWithLong(Long id) {
        int removed = heap.removeElementsGreaterThanId(id);
        System.out.println("Removed " + removed + " element(s) with id > " + id + ".");
    }
}
