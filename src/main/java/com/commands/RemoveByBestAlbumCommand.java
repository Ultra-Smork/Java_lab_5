package com.commands;
import com.utils.MinHeap;
import com.utils.Command;
public class RemoveByBestAlbumCommand implements Command{
    MinHeap heap = MinHeap.getInstance();
    @Override
    public void execute() {}
    public void executeWithString(String albumName){
        boolean removed = heap.removeElByBestAlbum(albumName);
        if (removed) {
            System.out.println("MusicBand(s) with best album '" + albumName + "' have been removed.");
        } else {
            System.out.println("No MusicBand found with best album '" + albumName + "'.");
        }
    }
}
