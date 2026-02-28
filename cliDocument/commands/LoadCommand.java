package com.commands;

import com.utils.MinHeap;
import com.utils.Command;
import com.utils.CollectionFileManager;

import java.util.List;
import java.util.Scanner;
import java.io.File;

/**
 * Command implementation for loading the collection from a file.
 * This command reads music band data from a file and populates the collection.
 * Can load from a custom file path or the default application file path.
 */
public class LoadCommand implements Command {
    /** Reference to the singleton MinHeap instance */
    private MinHeap heap = MinHeap.getInstance();
    /** Custom file path to load from, or null for default */
    private String customFilePath;

    /**
     * Constructs a LoadCommand that uses the default file path.
     */
    public LoadCommand() {
    }

    /**
     * Constructs a LoadCommand with a custom file path.
     *
     * @param customFilePath the path to the file to load from
     */
    public LoadCommand(String customFilePath) {
        this.customFilePath = customFilePath;
    }

    /**
     * Executes the load command, reading data from file and populating the collection.
     */
    @Override
    public void execute() {
        String filePath = customFilePath != null ? customFilePath : MinHeap.getFilePath();
        
        System.out.println("Loading from: " + filePath);
        
        CollectionFileManager.LoadResult result = CollectionFileManager.load(filePath);
        
        if (!result.isSuccess()) {
            System.out.println("Error: " + result.getErrorMessage());
            return;
        }

        heap.clear();
        
        if (!result.getBands().isEmpty()) {
            for (com.model.MusicBand band : result.getBands()) {
                heap.insert(band);
            }
            System.out.println("Successfully loaded " + result.getBands().size() + " elements.");
        } else {
            System.out.println("No elements found in file.");
        }

        for (String warning : result.getWarnings()) {
            System.out.println("Warning: " + warning);
        }
    }
}
