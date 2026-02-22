package com.utils;

import com.model.MusicBand;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class MinHeap {
    private static MinHeap instance;
    private static String defaultFilePath;
    private ArrayList<String> history;
    private PriorityQueue<MusicBand> heap;
    private LocalDateTime initializationDate;
    private String heapType;
    private List<String> metadataHistory;

    public MinHeap() {
        history = new ArrayList<String>();
        heap = new PriorityQueue<>();
        initializationDate = LocalDateTime.now();
        heapType = "MinHeap (PriorityQueue)";
        metadataHistory = new ArrayList<>();
        recordMetadata("Initialisation");
        
        if (defaultFilePath != null) {
            loadFromFile();
        }
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

    public static void setFilePath(String path) {
        defaultFilePath = path;
    }

    public static String getFilePath() {
        if (defaultFilePath == null) {
            defaultFilePath = System.getProperty("user.home") + File.separator + ".musicheap" + File.separator + "data.txt";
        }
        return defaultFilePath;
    }

    public void loadFromFile() {
        CollectionFileManager.LoadResult result = CollectionFileManager.load(getFilePath());
        
        if (!result.isSuccess()) {
            System.out.println("Warning: " + result.getErrorMessage());
            return;
        }

        heap.clear();
        
        if (!result.getBands().isEmpty()) {
            heap.addAll(result.getBands());
            System.out.println("Loaded " + result.getBands().size() + " elements from file.");
        }

        for (String warning : result.getWarnings()) {
            System.out.println("Warning: " + warning);
        }

        recordMetadata("Loaded from file");
    }

    public void saveToFile() {
        CollectionFileManager.SaveResult result = CollectionFileManager.save(
            new ArrayList<>(heap), 
            getFilePath()
        );
        
        if (result.isSuccess()) {
            System.out.println("Saved " + result.getSavedCount() + " elements to file: " + getFilePath());
            recordMetadata("Saved to file");
        } else {
            System.out.println("Error: " + result.getErrorMessage());
        }
    }

    public void insert(MusicBand band) {
        heap.offer(band);
    }

    public MusicBand extractMin() {
        return heap.poll();
    }

    public MusicBand peek() {
        return heap.peek();
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

    public boolean removeElById(Long id) {
        return heap.removeIf(band -> band.getId() == id);
    }

    public MusicBand findById(Long id) {
        for (MusicBand band : heap) {
            if (band.getId() == id) {
                return band;
            }
        }
        return null;
    }

    public void updateElement(MusicBand updatedBand) {
        heap.removeIf(band -> band.getId() == updatedBand.getId());
        heap.offer(updatedBand);
    }

    public boolean removeElByBestAlbum(String albumName) {
        return heap.removeIf(band -> band.getBestAlbum() != null && 
                              band.getBestAlbum().getName().equalsIgnoreCase(albumName));
    }

    public int removeElementsGreaterThanId(Long id) {
        int sizeBefore = heap.size();
        heap.removeIf(band -> band.getId() > id);
        return sizeBefore - heap.size();
    }

    public void clear() {
        heap.clear();
    }

    public void printHistory(){
        int start = Math.max(0, this.history.size() - 11);
        System.out.println(new ArrayList<>(this.history.subList(start, this.history.size())));
    }

    public void addToHistory(String cmd){
        this.history.add(cmd);
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

    public List<MusicBand> getAllElements() {
        return new ArrayList<>(heap);
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
        System.out.println("Save File Path: " + getFilePath());
        System.out.println("\n--- Metadata History ---");
        for (String entry : metadataHistory) {
            System.out.println(entry);
        }
        System.out.println("=================================\n");
    }
}
