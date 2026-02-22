package com.commands;

import com.utils.MinHeap;
import com.utils.Command;
import com.utils.CollectionFileManager;

import java.util.List;
import java.util.Scanner;
import java.io.File;

public class LoadCommand implements Command {
    private MinHeap heap = MinHeap.getInstance();
    private String customFilePath;

    public LoadCommand() {
    }

    public LoadCommand(String customFilePath) {
        this.customFilePath = customFilePath;
    }

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
