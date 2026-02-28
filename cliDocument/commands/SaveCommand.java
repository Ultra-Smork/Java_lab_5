package com.commands;

import com.utils.MinHeap;
import com.utils.Command;

/**
 * Command implementation for saving the collection to a file.
 * This command persists the current music band collection to the default
 * file path defined in the application's configuration.
 */
public class SaveCommand implements Command {
    /** Reference to the singleton MinHeap instance */
    private MinHeap heap = MinHeap.getInstance();

    /**
     * Executes the save command, writing the collection to file.
     */
    @Override
    public void execute() {
        heap.saveToFile();
    }
}
