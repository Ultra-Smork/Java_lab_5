package com.utils;

import com.model.MusicBand;

public class CommandManager {
    public static void HandleCommand(String command, MinHeap heap) {
        switch (command){
            case "V":
                listAllElements(heap);
                break;
            case "A":
                //we will add a new band with random data for testing purposes
                MusicBand newBand = new MusicBand("New Band", 60, 600);
                addElement(heap, newBand);
                System.out.println("Added new band: " + newBand);
                break;
            case "X":
                System.out.println("Exiting the application. Goodbye!");
                System.exit(0);
                break;
            case "S":
                printHeapInfo(heap);
                break;
            default:
                System.out.println("Unknown command. Please try again.");
        }
    }
    private static void listAllElements(MinHeap heap) {
        heap.printAll();
    }
    private static void addElement(MinHeap heap, MusicBand band) {
        heap.insert(band);
    }
    private static void printHeapInfo(MinHeap heap) {
        heap.printMetadata();
    }
}
