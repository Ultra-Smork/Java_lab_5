package com.utils;

import com.model.MusicBand;
import com.server.DatabaseManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Singleton class representing a MinHeap data structure for storing MusicBand objects.
 * This class implements the collection management functionality for the application.
 * 
 * <p>Data is loaded from PostgreSQL on initialization and persisted back to the database
 * only when save is explicitly called. All operations work with the in-memory heap.</p>
 */
public class MinHeap {
    private static MinHeap instance;
    private ArrayList<String> history;
    private PriorityQueue<MusicBand> heap;
    private LocalDateTime initializationDate;
    private String heapType;
    private List<String> metadataHistory;
    private List<String> startupWarnings;

    public MinHeap() {
        history = new ArrayList<String>();
        heap = new PriorityQueue<>();
        initializationDate = LocalDateTime.now();
        heapType = "MinHeap (PriorityQueue)";
        metadataHistory = new ArrayList<>();
        startupWarnings = new ArrayList<>();
        recordMetadata("Initialisation");
        
        loadFromDatabase();
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

    public static String getFilePath() {
        return "PostgreSQL Database";
    }

    public void loadFromDatabase() {
        try {
            ResultSet rs = DatabaseManager.executeQuery("SELECT * FROM music_bands");
            heap.clear();
            while (rs.next()) {
                MusicBand band = resultSetToBand(rs);
                heap.add(band);
            }
            rs.close();
            System.out.println("Loaded " + heap.size() + " elements from database");
        } catch (SQLException e) {
            System.out.println("Error loading from database: " + e.getMessage());
        }
        recordMetadata("Loaded from database");
    }

    public void saveToDatabase() {
        try {
            DatabaseManager.executeUpdate("DELETE FROM music_bands");
            for (MusicBand band : heap) {
                String sql = buildInsertSql(band);
                DatabaseManager.executeUpdate(sql);
            }
            System.out.println("Saved " + heap.size() + " elements to database");
        } catch (SQLException e) {
            System.out.println("Error saving to database: " + e.getMessage());
        }
        recordMetadata("Saved to database");
    }

    private String buildInsertSql(MusicBand band) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO music_bands (name, x, y, creation_date, number_of_participants, description, genre, album_name, album_sales) VALUES (");
        sql.append("'").append(escapeString(band.getName())).append("', ");
        sql.append(band.getCoordinates() != null ? band.getCoordinates().getX() : "NULL").append(", ");
        sql.append(band.getCoordinates() != null ? band.getCoordinates().getY() : "NULL").append(", ");
        if (band.getCreationDate() != null) {
            sql.append("'").append(band.getCreationDate()).append("', ");
        } else {
            sql.append("CURRENT_TIMESTAMP, ");
        }
        sql.append(band.getNumberOfParticipants()).append(", ");
        if (band.getDescription() != null) {
            sql.append("'").append(escapeString(band.getDescription())).append("', ");
        } else {
            sql.append("NULL, ");
        }
        if (band.getGenre() != null) {
            sql.append("'").append(band.getGenre().name()).append("', ");
        } else {
            sql.append("NULL, ");
        }
        if (band.getBestAlbum() != null) {
            sql.append("'").append(escapeString(band.getBestAlbum().getName())).append("', ");
            sql.append(band.getBestAlbum().getSales());
        } else {
            sql.append("NULL, NULL");
        }
        sql.append(")");
        return sql.toString();
    }

    private MusicBand resultSetToBand(ResultSet rs) throws SQLException {
        MusicBand band = new MusicBand();
        band.setId(rs.getLong("id"));
        band.setName(rs.getString("name"));
        
        long x = rs.getLong("x");
        int y = rs.getInt("y");
        if (!rs.wasNull()) {
            band.setCoordinates(new com.model.Coordinates(x, y));
        }
        
        band.setCreationDate(new java.util.Date(rs.getTimestamp("creation_date").getTime()));
        band.setNumberOfParticipants(rs.getInt("number_of_participants"));
        
        String desc = rs.getString("description");
        if (desc != null) band.setDescription(desc);
        
        String genre = rs.getString("genre");
        if (genre != null) {
            band.setGenre(com.model.MusicGenre.valueOf(genre));
        }
        
        String albumName = rs.getString("album_name");
        double albumSales = rs.getDouble("album_sales");
        if (!rs.wasNull()) {
            band.setBestAlbum(new com.model.Album(albumName, albumSales));
        }
        
        return band;
    }

    private String escapeString(String value) {
        if (value == null) return "";
        return value.replace("'", "''");
    }

    public void loadFromFile() {
        loadFromDatabase();
    }

    public void saveToFile() {
        saveToDatabase();
    }

    public void saveToFileSilently() {
        saveToDatabase();
    }

    public void loadFromFileSilently() {
        loadFromDatabase();
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

    public List<String> getAndClearStartupWarnings() {
        List<String> warnings = new ArrayList<>(startupWarnings);
        startupWarnings.clear();
        return warnings;
    }

    public void printMetadata() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("\n========== HEAP METADATA ==========");
        System.out.println("Heap Type: " + heapType);
        System.out.println("Date of Initialization: " + initializationDate.format(formatter));
        System.out.println("Amount of Elements: " + heap.size());
        System.out.println("Is Empty: " + heap.isEmpty());
        System.out.println("Data Source: " + getFilePath());
        System.out.println("\n--- Metadata History ---");
        for (String entry : metadataHistory) {
            System.out.println(entry);
        }
        System.out.println("=================================\n");
    }
}
