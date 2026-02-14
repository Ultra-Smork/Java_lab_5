package com.utils;

import java.awt.desktop.SystemEventListener;
import java.util.Scanner;

import com.model.MusicBand;

public class CommandManager {
    public static void HandleCommand(String command, MinHeap heap) {
        switch (command){
            case "V":
                listAllElements(heap);
                break;
            case "A":
                Scanner scanner = new Scanner(System.in);
                System.out.println("Enter band details in the format: name;numberOfMembers;genre");
                String data = scanner.nextLine();
                MusicBand newBand = MusicBand.fromString(data);
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
