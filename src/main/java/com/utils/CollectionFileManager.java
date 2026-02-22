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

public class CollectionFileManager {

    private static final String FIELD_DELIMITER = ";;";
    private static final String BAND_DELIMITER = "---";

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

    private static String escapeValue(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }

    private static String unescapeValue(String value) {
        if (value == null) return "";
        return value.replace("\\r", "\r")
                    .replace("\\n", "\n")
                    .replace("\\\\", "\\");
    }

    public static class SaveResult {
        private final boolean success;
        private final String errorMessage;
        private final int savedCount;

        private SaveResult(boolean success, String errorMessage, int savedCount) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.savedCount = savedCount;
        }

        public static SaveResult success(int count) {
            return new SaveResult(true, null, count);
        }

        public static SaveResult error(String message) {
            return new SaveResult(false, message, 0);
        }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public int getSavedCount() { return savedCount; }
    }

    public static class LoadResult {
        private final boolean success;
        private final String errorMessage;
        private final List<MusicBand> bands;
        private final List<String> warnings;

        private LoadResult(boolean success, String errorMessage, List<MusicBand> bands, List<String> warnings) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.bands = bands;
            this.warnings = warnings;
        }

        public static LoadResult success(List<MusicBand> bands, List<String> warnings) {
            return new LoadResult(true, null, bands, warnings);
        }

        public static LoadResult error(String message) {
            return new LoadResult(false, message, new ArrayList<>(), new ArrayList<>());
        }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public List<MusicBand> getBands() { return bands; }
        public List<String> getWarnings() { return warnings; }
    }
}
