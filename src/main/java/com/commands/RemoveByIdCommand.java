package com.commands;
import com.utils.MinHeap;
import com.utils.Command;
public class RemoveByIdCommand implements Command{
    MinHeap heap = MinHeap.getInstance();
    @Override
    public void execute() {}
    public void executeWithInt(Long elementId){
        boolean removed = heap.removeElById(elementId);
        if (removed) {
            System.out.println("MusicBand with id " + elementId + " has been removed.");
        } else {
            System.out.println("No MusicBand found with id " + elementId + ".");
        }
    }
}
