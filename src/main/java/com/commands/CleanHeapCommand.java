package com.commands;
import com.utils.MinHeap;
import com.utils.Command;
public class CleanHeapCommand implements Command{
    MinHeap heap = MinHeap.getInstance();
    @Override
    public void execute() {
        heap.clear();
        System.out.println("All elements deleted");
    }
}
