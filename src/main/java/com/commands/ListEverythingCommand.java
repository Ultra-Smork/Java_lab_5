package com.commands;
import com.utils.MinHeap;
import com.utils.Command;
public class ListEverythingCommand implements Command{
    MinHeap heap = MinHeap.getInstance();
    @Override
    public void execute() {
        heap.printAll();
    }
}

