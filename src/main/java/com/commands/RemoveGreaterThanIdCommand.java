package com.commands;
import com.utils.MinHeap;
import com.utils.Command;
public class RemoveGreaterThanIdCommand implements Command {
    MinHeap heap = MinHeap.getInstance();
    @Override
    public void execute() {}
    public void executeWithLong(Long id) {
        int removed = heap.removeElementsGreaterThanId(id);
        System.out.println("Removed " + removed + " element(s) with id > " + id + ".");
    }
}
