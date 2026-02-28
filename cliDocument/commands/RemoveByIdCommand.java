package com.commands;
import com.utils.MinHeap;
import com.utils.Command;

/**
 * Command implementation for removing a music band from the collection by its ID.
 * This command searches for a music band with the specified ID and removes it
 * from the collection's heap data structure.
 */
public class RemoveByIdCommand implements Command{
    /** Reference to the singleton MinHeap instance */
    MinHeap heap = MinHeap.getInstance();
    
    /**
     * Executes the command - does nothing (use executeWithInt instead).
     */
    @Override
    public void execute() {}
    
    /**
     * Removes a music band from the collection by its unique ID.
     *
     * @param elementId the unique identifier of the music band to remove
     */
    @Override
    public void executeWithInt(Long elementId){
        boolean removed = heap.removeElById(elementId);
        if (removed) {
            System.out.println("MusicBand with id " + elementId + " has been removed.");
        } else {
            System.out.println("No MusicBand found with id " + elementId + ".");
        }
    }
}
