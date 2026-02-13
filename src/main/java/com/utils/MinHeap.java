package com.utils;

import com.model.MusicBand;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class MinHeap {
    private static MinHeap instance;
    private PriorityQueue<MusicBand> heap;
    private LocalDateTime initializationDate;
    private String heapType;
    private List<String> metadataHistory;

    private MinHeap() {
        heap = new PriorityQueue<>();
        initializationDate = LocalDateTime.now();
        heapType = "MinHeap (PriorityQueue)";
        metadataHistory = new ArrayList<>();
        recordMetadata("Initialisation");
        // Placeholder data – consider loading from a file instead
        heap.offer(new MusicBand("Band A", 10, 100));
        heap.offer(new MusicBand("Band B", 20, 200));
        heap.offer(new MusicBand("Band C", 30, 300));
        heap.offer(new MusicBand("Band D", 40, 400));
        heap.offer(new MusicBand("Band E", 50, 500));
    }

    public static MinHeap getInstance() {
        if (instance == null) {
            synchronized (MinHeap.class) {
                if (instance == null) {
                    instance = new MinHeap();
                }
            }
        }
        return instance;
    }

    public void insert(MusicBand band) {
        heap.offer(band);
    }

    public MusicBand extractMin() {
        return heap.poll();
    }

    public void printAll() {
        if (heap.isEmpty()) {
            System.out.println("No elements in the collection.");
            return;
        }
        System.out.println("\n========== MUSIC BAND COLLECTION ==========\n");
        for (MusicBand band : heap) {
            System.out.println(band);
        }
    }

    public void removeElById(int id) {
        heap.removeIf(band -> band.getId() == id);
    }

    public LocalDateTime getInitializationDate() {
        return initializationDate;
    }

    public String getHeapType() {
        return heapType;
    }

    public int getElementCount() {
        return heap.size();
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public List<String> getMetadataHistory() {
        return new ArrayList<>(metadataHistory);
    }

    private void recordMetadata(String action) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String entry = String.format("[%s] %s - Elements: %d",
            now.format(formatter), action, heap.size());
        metadataHistory.add(entry);
    }

    public void printMetadata() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("\n========== HEAP METADATA ==========");
        System.out.println("Heap Type: " + heapType);
        System.out.println("Date of Initialization: " + initializationDate.format(formatter));
        System.out.println("Amount of Elements: " + heap.size());
        System.out.println("Is Empty: " + heap.isEmpty());
        System.out.println("\n--- Metadata History ---");
        for (String entry : metadataHistory) {
            System.out.println(entry);
        }
        System.out.println("=================================\n");
    }
}
