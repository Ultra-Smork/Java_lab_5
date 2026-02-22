package com.commands;

import com.utils.MinHeap;
import com.utils.Command;

public class SaveCommand implements Command {
    private MinHeap heap = MinHeap.getInstance();

    @Override
    public void execute() {
        heap.saveToFile();
    }
}
