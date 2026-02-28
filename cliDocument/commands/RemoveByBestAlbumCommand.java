package com.commands;
import com.utils.MinHeap;
import com.utils.Command;

/**
 * Command implementation for removing a music band by its best album name.
 * This command searches for a music band with the specified best album name
 * and removes the first matching element from the collection.
 */
public class RemoveByBestAlbumCommand implements Command{
    /** Reference to the singleton MinHeap instance */
    MinHeap heap = MinHeap.getInstance();
    
    /**
     * Executes the command - does nothing (use executeWithString instead).
     */
    @Override
    public void execute() {}
    
    /**
     * Removes the first music band with the specified best album name.
     *
     * @param albumName the name of the best album to search for
     */
    @Override
    public void executeWithString(String albumName){
        boolean removed = heap.removeElByBestAlbum(albumName);
        if (removed) {
            System.out.println("MusicBand(s) with best album '" + albumName + "' have been removed.");
        } else {
            System.out.println("No MusicBand found with best album '" + albumName + "'.");
        }
    }
}
