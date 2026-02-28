package com.utils;

import com.model.MusicBand;
import com.model.Album;
import com.model.Coordinates;
import com.model.MusicGenre;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Manages file operations for loading and saving the music band collection.
 * This class handles reading from and writing to CSV files, path resolution,
 * and environment configuration management.
 */
public class CollectionFileManager {

    /** Delimiter used to separate field key-value pairs */
    private static final String FIELD_DELIMITER = ";;";
    /** Delimiter used to separate individual music bands in the file */
    private static final String BAND_DELIMITER = "---";

    /**
     * Saves the list of music bands to the specified file path.
     * The data is stored in a custom format using field delimiters and band delimiters.
     *
     * @param bands    the list of music bands to save
     * @param filePath the path to save the file to
     * @return a SaveResult object indicating success or failure with details
     */
    public static synchronized SaveResult save(List<MusicBand> bands, String filePath) {
        if (bands == null || filePath == null || filePath.trim().isEmpty()) {
            return SaveResult.error("Invalid bands list or file path.");
        }

        File file = new File(filePath);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                return SaveResult.error("Failed to create directory: " + parentDir.getAbsolutePath());
            }
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            StringBuilder sb = new StringBuilder();

            for (MusicBand band : bands) {
                if (band == null) {
                    continue;
                }

                sb.append("id").append(FIELD_DELIMITER).append(band.getId()).append("\n");
                sb.append("name").append(FIELD_DELIMITER).append(escapeValue(band.getName())).append("\n");
                sb.append("x").append(FIELD_DELIMITER).append(band.getCoordinates() != null ? band.getCoordinates().getX() : "").append("\n");
                sb.append("y").append(FIELD_DELIMITER).append(band.getCoordinates() != null ? band.getCoordinates().getY() : "").append("\n");
                sb.append("numberOfParticipants").append(FIELD_DELIMITER).append(band.getNumberOfParticipants() != null ? band.getNumberOfParticipants() : "").append("\n");
                sb.append("description").append(FIELD_DELIMITER).append(escapeValue(band.getDescription())).append("\n");
                sb.append("genre").append(FIELD_DELIMITER).append(band.getGenre() != null ? band.getGenre().name() : "").append("\n");
                
                if (band.getBestAlbum() != null) {
                    sb.append("album_name").append(FIELD_DELIMITER).append(escapeValue(band.getBestAlbum().getName())).append("\n");
                    sb.append("album_sales").append(FIELD_DELIMITER).append(band.getBestAlbum().getSales() != null ? band.getBestAlbum().getSales() : "").append("\n");
                } else {
                    sb.append("album_name").append(FIELD_DELIMITER).append("").append("\n");
                    sb.append("album_sales").append(FIELD_DELIMITER).append("").append("\n");
                }

                sb.append(BAND_DELIMITER).append("\n");
            }

            fos.write(sb.toString().getBytes("UTF-8"));
            return SaveResult.success(bands.size());

        } catch (SecurityException e) {
            return SaveResult.error("Permission denied: Cannot write to file. " + e.getMessage());
        } catch (Exception e) {
            return SaveResult.error("Failed to save: " + e.getMessage());
        }
    }

    /**
     * Loads the music band collection from the specified file path.
     * Parses the custom format and reconstructs MusicBand objects.
     *
     * @param filePath the path to the file to load from
     * @return a LoadResult object containing the loaded bands or error information
     */
    public static synchronized LoadResult load(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return LoadResult.error("Invalid file path.");
        }

        File file = new File(filePath);

        if (!file.exists()) {
            return LoadResult.error("File not found: " + filePath);
        }

        if (!file.canRead()) {
            return LoadResult.error("Permission denied: Cannot read file. " + filePath);
        }

        List<MusicBand> bands = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try (Scanner scanner = new Scanner(file, "UTF-8")) {
            MusicBand currentBand = null;
            String currentAlbumName = null;
            String currentAlbumSales = null;
            int lineNumber = 0;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                if (line.trim().equals(BAND_DELIMITER)) {
                    if (currentBand != null) {
                        if (currentAlbumName != null && !currentAlbumName.isEmpty()) {
                            try {
                                Album album = new Album(
                                    unescapeValue(currentAlbumName),
                                    currentAlbumSales != null && !currentAlbumSales.isEmpty() ? Double.parseDouble(currentAlbumSales) : 0.0
                                );
                                if (album.getSales() <= 0) {
                                    warnings.add("Line " + lineNumber + ": Invalid album sales, skipping album.");
                                } else {
                                    currentBand.setBestAlbum(album);
                                }
                            } catch (NumberFormatException e) {
                                warnings.add("Line " + lineNumber + ": Invalid album sales format.");
                            }
                        }

                        if (validateBand(currentBand)) {
                            bands.add(currentBand);
                        } else {
                            warnings.add("Line " + lineNumber + ": Invalid band data, skipping.");
                        }
                    }
                    currentBand = null;
                    currentAlbumName = null;
                    currentAlbumSales = null;
                    continue;
                }

                String[] parts = line.split(FIELD_DELIMITER, 2);
                if (parts.length != 2) {
                    continue;
                }

                String key = parts[0].trim();
                String value = parts[1];

                if (currentBand == null) {
                    currentBand = new MusicBand();
                }

                try {
                    switch (key) {
                        case "id":
                            long id = Long.parseLong(value);
                            if (id <= 0) {
                                warnings.add("Line " + lineNumber + ": Invalid ID, must be > 0.");
                            }
                            currentBand.setId(id);
                            break;
                        case "name":
                            String name = unescapeValue(value);
                            if (name == null || name.isEmpty()) {
                                warnings.add("Line " + lineNumber + ": Name cannot be empty.");
                            }
                            currentBand.setName(name);
                            break;
                        case "x":
                            if (!value.isEmpty()) {
                                long x = Long.parseLong(value);
                                if (x > 554) {
                                    warnings.add("Line " + lineNumber + ": X coordinate > 554, will be rejected.");
                                }
                                if (currentBand.getCoordinates() == null) {
                                    currentBand.setCoordinates(new Coordinates(x, 0));
                                } else {
                                    currentBand.setCoordinates(new Coordinates(x, currentBand.getCoordinates().getY()));
                                }
                            }
                            break;
                        case "y":
                            if (!value.isEmpty()) {
                                int y = Integer.parseInt(value);
                                if (y > 782) {
                                    warnings.add("Line " + lineNumber + ": Y coordinate > 782, will be rejected.");
                                }
                                if (currentBand.getCoordinates() == null) {
                                    currentBand.setCoordinates(new Coordinates(0L, y));
                                } else {
                                    currentBand.setCoordinates(new Coordinates(currentBand.getCoordinates().getX(), y));
                                }
                            }
                            break;
                        case "numberOfParticipants":
                            if (!value.isEmpty()) {
                                int participants = Integer.parseInt(value);
                                if (participants <= 0) {
                                    warnings.add("Line " + lineNumber + ": Number of participants must be > 0.");
                                }
                                currentBand.setNumberOfParticipants(participants);
                            }
                            break;
                        case "description":
                            currentBand.setDescription(value.isEmpty() ? null : unescapeValue(value));
                            break;
                        case "genre":
                            if (!value.isEmpty()) {
                                try {
                                    MusicGenre genre = MusicGenre.valueOf(value);
                                    if (genre != MusicGenre.PLACEHOLDER) {
                                        currentBand.setGenre(genre);
                                    }
                                } catch (IllegalArgumentException e) {
                                    warnings.add("Line " + lineNumber + ": Invalid genre '" + value + "'.");
                                }
                            }
                            break;
                        case "album_name":
                            currentAlbumName = value;
                            break;
                        case "album_sales":
                            currentAlbumSales = value;
                            break;
                    }
                } catch (NumberFormatException e) {
                    warnings.add("Line " + lineNumber + ": Invalid number format for '" + key + "'.");
                }
            }

            if (currentBand != null) {
                if (currentAlbumName != null && !currentAlbumName.isEmpty()) {
                    try {
                        Album album = new Album(
                            unescapeValue(currentAlbumName),
                            currentAlbumSales != null && !currentAlbumSales.isEmpty() ? Double.parseDouble(currentAlbumSales) : 0.0
                        );
                        if (album.getSales() <= 0) {
                            warnings.add("Line " + lineNumber + ": Invalid album sales, skipping album.");
                        } else {
                            currentBand.setBestAlbum(album);
                        }
                    } catch (NumberFormatException e) {
                        warnings.add("Line " + lineNumber + ": Invalid album sales format.");
                    }
                }

                if (validateBand(currentBand)) {
                    bands.add(currentBand);
                } else {
                    warnings.add("Line " + lineNumber + ": Invalid band data, skipping.");
                }
            }

        } catch (FileNotFoundException e) {
            return LoadResult.error("File not found: " + filePath);
        } catch (Exception e) {
            return LoadResult.error("Failed to read file: " + e.getMessage());
        }

        return LoadResult.success(bands, warnings);
    }

    /**
     * Validates that a music band has all required fields with valid values.
     *
     * @param band the music band to validate
     * @return true if the band is valid, false otherwise
     */
    private static boolean validateBand(MusicBand band) {
        if (band == null) return false;
        if (band.getId() <= 0) return false;
        if (band.getName() == null || band.getName().isEmpty()) return false;
        if (band.getCoordinates() == null) return false;
        if (band.getNumberOfParticipants() == null || band.getNumberOfParticipants() <= 0) return false;
        if (band.getBestAlbum() == null) return false;
        if (band.getBestAlbum().getName() == null || band.getBestAlbum().getName().isEmpty()) return false;
        if (band.getBestAlbum().getSales() == null || band.getBestAlbum().getSales() <= 0) return false;
        return true;
    }

    /**
     * Escapes special characters in a string value for safe file storage.
     * Handles backslashes, newlines, and carriage returns.
     *
     * @param value the string to escape
     * @return the escaped string
     */
    private static String escapeValue(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }

    /**
     * Unescapes special characters in a string value that was previously escaped.
     *
     * @param value the string to unescape
     * @return the unescaped string
     */
    private static String unescapeValue(String value) {
        if (value == null) return "";
        return value.replace("\\r", "\r")
                    .replace("\\n", "\n")
                    .replace("\\\\", "\\");
    }

    /**
     * Gets the file path to the environment configuration file.
     * The .env file contains paths to data files.
     *
     * @return the path to the environment file
     */
    public static String getEnvFilePath() {
        return System.getProperty("user.home") + File.separator + ".musicheap" + File.separator + ".env";
    }

    /**
     * Reads the list of data file paths from the environment configuration file.
     * Lines starting with # are treated as comments and ignored.
     *
     * @return a list of file paths to use for data storage
     */
    public static List<String> readDataPathsFromEnv() {
        List<String> paths = new ArrayList<>();
        String envPath = getEnvFilePath();
        File envFile = new File(envPath);

        if (!envFile.exists()) {
            createDefaultEnvFile();
            return readDataPathsFromEnv();
        }

        try (Scanner scanner = new Scanner(envFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String resolvedPath = resolvePath(line);
                    if (resolvedPath != null) {
                        paths.add(resolvedPath);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not read .env file: " + e.getMessage());
        }

        if (paths.isEmpty()) {
            String defaultPath = resolvePath(System.getProperty("user.home") + File.separator + ".musicheap" + File.separator + "data.csv");
            if (defaultPath != null) {
                paths.add(defaultPath);
            }
        }

        return paths;
    }

    /**
     * Creates a default environment configuration file with default data paths.
     * This is called automatically if no .env file exists.
     */
    public static void createDefaultEnvFile() {
        String envPath = getEnvFilePath();
        File envFile = new File(envPath);

        if (envFile.exists()) {
            return;
        }

        try {
            File musicheapDir = new File(System.getProperty("user.home") + File.separator + ".musicheap");
            if (!musicheapDir.exists()) {
                musicheapDir.mkdirs();
            }

            String defaultPath1 = System.getProperty("user.home") + File.separator + ".musicheap" + File.separator + "data.csv";
            String defaultPath2 = System.getProperty("user.home") + File.separator + ".musicheap" + File.separator + "backup.csv";

            StringBuilder sb = new StringBuilder();
            sb.append("# Music Band Collection Data File Paths\n");
            sb.append("# One file path per line. First writable path will be used for saving.\n");
            sb.append("# Auto-generated file - do not delete\n");
            sb.append(defaultPath1).append("\n");
            sb.append(defaultPath2).append("\n");

            try (FileOutputStream fos = new FileOutputStream(envFile)) {
                fos.write(sb.toString().getBytes("UTF-8"));
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not create .env file: " + e.getMessage());
        }
    }

    /**
     * Resolves a file path, expanding the tilde (~) to the user's home directory.
     *
     * @param path the path to resolve
     * @return the resolved absolute path, or null if the input is invalid
     */
    public static String resolvePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }

        String resolved = path.trim();
        if (resolved.startsWith("~" + File.separator)) {
            resolved = System.getProperty("user.home") + resolved.substring(1);
        } else if (resolved.startsWith("~")) {
            resolved = System.getProperty("user.home") + resolved.substring(1);
        }

        return resolved;
    }

    /**
     * Gets the first valid (readable) file path from the configuration.
     * Returns the first path that exists and is readable.
     *
     * @return the first valid file path, or a default path if none exist
     */
    public static String getFirstValidPath() {
        List<String> paths = readDataPathsFromEnv();
        for (String path : paths) {
            File file = new File(path);
            if (file.exists() && file.canRead()) {
                return path;
            }
        }
        if (!paths.isEmpty()) {
            return paths.get(0);
        }
        return resolvePath(System.getProperty("user.home") + File.separator + ".musicheap" + File.separator + "data.csv");
    }

    /**
     * Gets the first writable file path from the configuration.
     * Returns the first path that either exists and is writable, or has a writable parent directory.
     *
     * @return the first writable file path, or a default path if none are writable
     */
    public static String getFirstWritablePath() {
        List<String> paths = readDataPathsFromEnv();
        for (String path : paths) {
            File file = new File(path);
            File parentDir = file.getParentFile();
            if ((file.exists() && file.canWrite()) || (parentDir != null && parentDir.canWrite())) {
                return path;
            }
        }
        if (!paths.isEmpty()) {
            return paths.get(0);
        }
        return resolvePath(System.getProperty("user.home") + File.separator + ".musicheap" + File.separator + "data.csv");
    }

    /**
     * Result object for save operations containing success status and details.
     */
    public static class SaveResult {
        /** Whether the save operation was successful */
        private final boolean success;
        /** Error message if the operation failed */
        private final String errorMessage;
        /** Number of bands that were saved */
        private final int savedCount;

        /**
         * Constructs a SaveResult with the given parameters.
         *
         * @param success     whether the save was successful
         * @param errorMessage error message if failed
         * @param savedCount  number of bands saved
         */
        private SaveResult(boolean success, String errorMessage, int savedCount) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.savedCount = savedCount;
        }

        /**
         * Creates a successful save result.
         *
         * @param count the number of bands saved
         * @return a successful SaveResult
         */
        public static SaveResult success(int count) {
            return new SaveResult(true, null, count);
        }

        /**
         * Creates an error save result.
         *
         * @param message the error message
         * @return an error SaveResult
         */
        public static SaveResult error(String message) {
            return new SaveResult(false, message, 0);
        }

        /**
         * Checks if the save operation was successful.
         *
         * @return true if successful, false otherwise
         */
        public boolean isSuccess() { return success; }
        
        /**
         * Gets the error message if the operation failed.
         *
         * @return the error message, or null if successful
         */
        public String getErrorMessage() { return errorMessage; }
        
        /**
         * Gets the number of bands that were saved.
         *
         * @return the saved count
         */
        public int getSavedCount() { return savedCount; }
    }

    /**
     * Result object for load operations containing success status, loaded bands, and warnings.
     */
    public static class LoadResult {
        /** Whether the load operation was successful */
        private final boolean success;
        /** Error message if the operation failed */
        private final String errorMessage;
        /** List of music bands that were loaded */
        private final List<MusicBand> bands;
        /** List of warnings encountered during loading */
        private final List<String> warnings;

        /**
         * Constructs a LoadResult with the given parameters.
         *
         * @param success     whether the load was successful
         * @param errorMessage error message if failed
         * @param bands       list of loaded bands
         * @param warnings    list of warnings
         */
        private LoadResult(boolean success, String errorMessage, List<MusicBand> bands, List<String> warnings) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.bands = bands;
            this.warnings = warnings;
        }

        /**
         * Creates a successful load result.
         *
         * @param bands    list of loaded bands
         * @param warnings list of warnings
         * @return a successful LoadResult
         */
        public static LoadResult success(List<MusicBand> bands, List<String> warnings) {
            return new LoadResult(true, null, bands, warnings);
        }

        /**
         * Creates an error load result.
         *
         * @param message the error message
         * @return an error LoadResult
         */
        public static LoadResult error(String message) {
            return new LoadResult(false, message, new ArrayList<>(), new ArrayList<>());
        }

        /**
         * Checks if the load operation was successful.
         *
         * @return true if successful, false otherwise
         */
        public boolean isSuccess() { return success; }
        
        /**
         * Gets the error message if the operation failed.
         *
         * @return the error message, or null if successful
         */
        public String getErrorMessage() { return errorMessage; }
        
        /**
         * Gets the list of music bands that were loaded.
         *
         * @return the list of bands
         */
        public List<MusicBand> getBands() { return bands; }
        
        /**
         * Gets the list of warnings encountered during loading.
         *
         * @return the list of warnings
         */
        public List<String> getWarnings() { return warnings; }
    }
}
