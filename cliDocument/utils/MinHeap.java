package com.utils;

import com.model.MusicBand;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Singleton class representing a MinHeap data structure for storing MusicBand objects.
 * This class implements the collection management functionality for the application,
 * providing methods to add, remove, update, and query elements in the collection.
 * 
 * <p>The heap is implemented using Java's PriorityQueue and maintains:</p>
 * <ul>
 *   <li>Command history for the last 11 commands</li>
 *   <li>Metadata history of collection operations</li>
 *   <li>Automatic persistence to/from file</li>
 * </ul>
 */
public class MinHeap {
    /** Singleton instance of the MinHeap */
    private static MinHeap instance;
    /** History of commands executed (last 11) */
    private ArrayList<String> history;
    /** The priority queue storing MusicBand elements */
    private PriorityQueue<MusicBand> heap;
    /** The date and time when the collection was initialized */
    private LocalDateTime initializationDate;
    /** Description of the heap type */
    private String heapType;
    /** History of metadata changes to the collection */
    private List<String> metadataHistory;

    /**
     * Constructs a new MinHeap instance.
     * Initializes the heap, history, and loads data from file.
     * Private constructor - use getInstance() to obtain the singleton.
     */
    public MinHeap() {
        history = new ArrayList<String>();
        heap = new PriorityQueue<>();
        initializationDate = LocalDateTime.now();
        heapType = "MinHeap (PriorityQueue)";
        metadataHistory = new ArrayList<>();
        recordMetadata("Initialisation");
        
        loadFromFile();
    }

    /**
     * Gets the singleton instance of the MinHeap.
     * Uses double-checked locking for thread safety.
     *
     * @return the singleton MinHeap instance
     */
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

    /**
     * Gets the file path used for data persistence.
     *
     * @return the path to the data file
     */
    public static String getFilePath() {
        return CollectionFileManager.getFirstValidPath();
    }

    /**
     * Loads the collection from the default file path.
     * Displays appropriate messages for success, warnings, or errors.
     */
    public void loadFromFile() {
        String filePath = CollectionFileManager.getFirstValidPath();
        CollectionFileManager.LoadResult result = CollectionFileManager.load(filePath);
        
        if (!result.isSuccess()) {
            System.out.println("No saved data found. Starting with empty collection.");
            return;
        }

        heap.clear();
        
        if (!result.getBands().isEmpty()) {
            heap.addAll(result.getBands());
            System.out.println("Loaded " + result.getBands().size() + " elements from: " + filePath);
        } else {
            System.out.println("No saved data found. Starting with empty collection.");
        }

        for (String warning : result.getWarnings()) {
            System.out.println("Warning: " + warning);
        }

        recordMetadata("Loaded from file");
    }

    /**
     * Saves the collection to the default file path.
     * Outputs success or error messages to standard output.
     */
    public void saveToFile() {
        String filePath = CollectionFileManager.getFirstWritablePath();
        CollectionFileManager.SaveResult result = CollectionFileManager.save(
            new ArrayList<>(heap), 
            filePath
        );
        
        if (result.isSuccess()) {
            System.out.println("Saved " + result.getSavedCount() + " elements to: " + filePath);
            recordMetadata("Saved to file");
        } else {
            System.out.println("Error: " + result.getErrorMessage());
        }
    }

    /**
     * Inserts a new music band into the collection.
     *
     * @param band the MusicBand to insert
     */
    public void insert(MusicBand band) {
        heap.offer(band);
    }

    /**
     * Removes and returns the minimum element from the collection.
     *
     * @return the minimum MusicBand, or null if the collection is empty
     */
    public MusicBand extractMin() {
        return heap.poll();
    }

    /**
     * Returns the minimum element without removing it.
     *
     * @return the minimum MusicBand, or null if the collection is empty
     */
    public MusicBand peek() {
        return heap.peek();
    }

    /**
     * Prints all elements in the collection to standard output.
     * Each element is printed using its toString() representation.
     */
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

    /**
     * Removes a music band from the collection by its unique ID.
     *
     * @param id the unique identifier of the band to remove
     * @return true if an element was removed, false otherwise
     */
    public boolean removeElById(Long id) {
        return heap.removeIf(band -> band.getId() == id);
    }

    /**
     * Finds a music band in the collection by its unique ID.
     *
     * @param id the unique identifier to search for
     * @return the MusicBand if found, null otherwise
     */
    public MusicBand findById(Long id) {
        for (MusicBand band : heap) {
            if (band.getId() == id) {
                return band;
            }
        }
        return null;
    }

    /**
     * Updates an existing music band in the collection with new values.
     * Removes the old element and inserts the updated one.
     *
     * @param updatedBand the MusicBand with updated values (matched by ID)
     */
    public void updateElement(MusicBand updatedBand) {
        heap.removeIf(band -> band.getId() == updatedBand.getId());
        heap.offer(updatedBand);
    }

    /**
     * Removes a music band from the collection based on its best album name.
     * Only removes the first matching element.
     *
     * @param albumName the name of the best album to search for
     * @return true if an element was removed, false otherwise
     */
    public boolean removeElByBestAlbum(String albumName) {
        return heap.removeIf(band -> band.getBestAlbum() != null && 
                              band.getBestAlbum().getName().equalsIgnoreCase(albumName));
    }

    /**
     * Removes all music bands from the collection that have an ID greater than the specified value.
     *
     * @param id the ID threshold - all bands with ID greater than this will be removed
     * @return the number of elements removed
     */
    public int removeElementsGreaterThanId(Long id) {
        int sizeBefore = heap.size();
        heap.removeIf(band -> band.getId() > id);
        return sizeBefore - heap.size();
    }

    /**
     * Removes all elements from the collection.
     */
    public void clear() {
        heap.clear();
    }

    /**
     * Prints the history of the last 11 commands executed.
     */
    public void printHistory(){
        int start = Math.max(0, this.history.size() - 11);
        System.out.println(new ArrayList<>(this.history.subList(start, this.history.size())));
    }

    /**
     * Adds a command to the command history.
     *
     * @param cmd the command string to add to history
     */
    public void addToHistory(String cmd){
        this.history.add(cmd);
    }

    /**
     * Gets the date and time when the collection was initialized.
     *
     * @return the initialization date and time
     */
    public LocalDateTime getInitializationDate() {
        return initializationDate;
    }

    /**
     * Gets the type description of the heap implementation.
     *
     * @return the heap type string
     */
    public String getHeapType() {
        return heapType;
    }

    /**
     * Gets the current number of elements in the collection.
     *
     * @return the element count
     */
    public int getElementCount() {
        return heap.size();
    }

    /**
     * Checks if the collection is empty.
     *
     * @return true if the collection contains no elements
     */
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    /**
     * Gets all elements in the collection as a list.
     * Returns a copy to prevent external modification of the internal heap.
     *
     * @return a list containing all music bands
     */
    public List<MusicBand> getAllElements() {
        return new ArrayList<>(heap);
    }

    /**
     * Counts the number of elements in the collection with the specified number of participants.
     *
     * @param numberOfParticipants the number of participants to count
     * @return the count of elements matching the criteria
     */
    public int countByNumberOfParticipants(int numberOfParticipants) {
        int count = 0;
        for (MusicBand band : heap) {
            if (band.getNumberOfParticipants() != null && 
                band.getNumberOfParticipants().equals(numberOfParticipants)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets the metadata history of collection operations.
     *
     * @return a list of metadata history entries
     */
    public List<String> getMetadataHistory() {
        return new ArrayList<>(metadataHistory);
    }

    /**
     * Records a metadata entry for the given action.
     *
     * @param action the action that triggered the metadata record
     */
    private void recordMetadata(String action) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String entry = String.format("[%s] %s - Elements: %d",
            now.format(formatter), action, heap.size());
        metadataHistory.add(entry);
    }

    /**
     * Prints detailed metadata about the collection including type, initialization date,
     * element count, and metadata history.
     */
    public void printMetadata() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("\n========== HEAP METADATA ==========");
        System.out.println("Heap Type: " + heapType);
        System.out.println("Date of Initialization: " + initializationDate.format(formatter));
        System.out.println("Amount of Elements: " + heap.size());
        System.out.println("Is Empty: " + heap.isEmpty());
        System.out.println("Data File Path: " + getFilePath());
        System.out.println("\n--- Metadata History ---");
        for (String entry : metadataHistory) {
            System.out.println(entry);
        }
        System.out.println("=================================\n");
    }
}
