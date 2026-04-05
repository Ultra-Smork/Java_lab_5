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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Singleton class representing a MinHeap data structure for storing MusicBand objects.
 * This class implements the collection management functionality for the application.
 * 
 * <p>Data is loaded from PostgreSQL on initialization and persisted back to the database
 * only when save is explicitly called. All operations work with the in-memory heap.</p>
 * 
 * <p>Uses ReentrantReadWriteLock for thread-safe access to the collection.</p>
 */
public class MinHeap {
    private static MinHeap instance;
    private ArrayList<String> history;
    private PriorityQueue<MusicBand> heap;
    private LocalDateTime initializationDate;
    private String heapType;
    private List<String> metadataHistory;
    private List<String> startupWarnings;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

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
        rwLock.writeLock().lock();
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
        } finally {
            rwLock.writeLock().unlock();
        }
        recordMetadata("Loaded from database");
    }

    public boolean saveToDatabase() {
        boolean success = true;
        rwLock.readLock().lock();
        try {
            DatabaseManager.executeUpdate("DELETE FROM music_bands");
            
            if (heap.isEmpty()) {
                System.out.println("Saved 0 elements to database");
                return true;
            }
            
            List<MusicBand> bands = new ArrayList<>(heap);
            ForkJoinPool pool = new ForkJoinPool();
            InsertBatchTask task = new InsertBatchTask(bands, 0, bands.size());
            pool.invoke(task);
            
            if (task.hasErrors()) {
                System.err.println("Some inserts failed during save");
                success = false;
            }
            
            System.out.println("Saved " + heap.size() + " elements to database");
        } catch (SQLException e) {
            System.out.println("Error saving to database: " + e.getMessage());
            success = false;
        } finally {
            rwLock.readLock().unlock();
        }
        recordMetadata("Saved to database");
        return success;
    }
    
    private class InsertBatchTask extends RecursiveAction {
        private final List<MusicBand> bands;
        private final int start;
        private final int end;
        private static final int THRESHOLD = 10;
        private volatile boolean hasError = false;
        
        public InsertBatchTask(List<MusicBand> bands, int start, int end) {
            this.bands = bands;
            this.start = start;
            this.end = end;
        }
        
        public boolean hasErrors() {
            return hasError;
        }
        
        @Override
        protected void compute() {
            if (end - start <= THRESHOLD) {
                for (int i = start; i < end; i++) {
                    try {
                        String sql = buildInsertSql(bands.get(i));
                        DatabaseManager.executeUpdate(sql);
                    } catch (SQLException e) {
                        System.err.println("Error inserting band: " + e.getMessage());
                        hasError = true;
                    }
                }
            } else {
                int mid = start + (end - start) / 2;
                InsertBatchTask left = new InsertBatchTask(bands, start, mid);
                InsertBatchTask right = new InsertBatchTask(bands, mid, end);
                invokeAll(left, right);
                if (left.hasErrors() || right.hasErrors()) {
                    hasError = true;
                }
            }
        }
    }

    private String buildInsertSql(MusicBand band) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO music_bands (name, x, y, creation_date, number_of_participants, description, genre, album_name, album_sales, owner_login, owner_password_hash) VALUES (");
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
        
        if (band.getOwnerLogin() != null) {
            sql.append(", '").append(escapeString(band.getOwnerLogin())).append("'");
            sql.append(", '").append(escapeString(band.getOwnerPasswordHash())).append("'");
        } else {
            sql.append(", NULL, NULL");
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
        
        String ownerLogin = rs.getString("owner_login");
        if (ownerLogin != null) {
            band.setOwnerLogin(ownerLogin);
            band.setOwnerPasswordHash(rs.getString("owner_password_hash"));
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
        rwLock.writeLock().lock();
        try {
            heap.offer(band);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public MusicBand extractMin() {
        rwLock.writeLock().lock();
        try {
            return heap.poll();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public MusicBand peek() {
        rwLock.readLock().lock();
        try {
            return heap.peek();
        } finally {
            rwLock.readLock().unlock();
        }
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
        rwLock.writeLock().lock();
        try {
            return heap.removeIf(band -> band.getId() == id);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public MusicBand findById(Long id) {
        rwLock.readLock().lock();
        try {
            for (MusicBand band : heap) {
                if (band.getId() == id) {
                    return band;
                }
            }
            return null;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void updateElement(MusicBand updatedBand) {
        rwLock.writeLock().lock();
        try {
            heap.removeIf(band -> band.getId() == updatedBand.getId());
            heap.offer(updatedBand);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public boolean removeElByBestAlbum(String albumName) {
        rwLock.writeLock().lock();
        try {
            return heap.removeIf(band -> band.getBestAlbum() != null && 
                                  band.getBestAlbum().getName().equalsIgnoreCase(albumName));
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public int removeElByBestAlbumOwned(String albumName, String ownerLogin) {
        rwLock.writeLock().lock();
        try {
            int sizeBefore = heap.size();
            heap.removeIf(band -> band.getBestAlbum() != null && 
                                  band.getBestAlbum().getName().equalsIgnoreCase(albumName) &&
                                  ownerLogin.equals(band.getOwnerLogin()));
            return sizeBefore - heap.size();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public int removeElementsGreaterThanId(Long id) {
        rwLock.writeLock().lock();
        try {
            int sizeBefore = heap.size();
            heap.removeIf(band -> band.getId() > id);
            return sizeBefore - heap.size();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public int removeElementsGreaterThanIdOwned(Long id, String ownerLogin) {
        rwLock.writeLock().lock();
        try {
            int sizeBefore = heap.size();
            heap.removeIf(band -> band.getId() > id && ownerLogin.equals(band.getOwnerLogin()));
            return sizeBefore - heap.size();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public void clear() {
        rwLock.writeLock().lock();
        try {
            heap.clear();
        } finally {
            rwLock.writeLock().unlock();
        }
    }
    
    public int clearOwned(String ownerLogin) {
        rwLock.writeLock().lock();
        try {
            int sizeBefore = heap.size();
            heap.removeIf(band -> ownerLogin.equals(band.getOwnerLogin()));
            return sizeBefore - heap.size();
        } finally {
            rwLock.writeLock().unlock();
        }
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
        rwLock.readLock().lock();
        try {
            return heap.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public boolean isEmpty() {
        rwLock.readLock().lock();
        try {
            return heap.isEmpty();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public List<MusicBand> getAllElements() {
        rwLock.readLock().lock();
        try {
            return new ArrayList<>(heap);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public int countByNumberOfParticipants(int numberOfParticipants) {
        rwLock.readLock().lock();
        try {
            int count = 0;
            for (MusicBand band : heap) {
                if (band.getNumberOfParticipants() != null && 
                    band.getNumberOfParticipants().equals(numberOfParticipants)) {
                    count++;
                }
            }
            return count;
        } finally {
            rwLock.readLock().unlock();
        }
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
