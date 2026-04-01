package com.client;

import com.model.MusicBand;
import com.model.Album;
import com.model.Coordinates;
import com.model.MusicGenre;

import java.sql.Timestamp;
import java.util.Date;

public class SqlQueryBuilder {

    public static String buildInsert(MusicBand band) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO music_bands (name, x, y, creation_date, number_of_participants, description, genre, album_name, album_sales) VALUES (");
        
        sql.append("'").append(escapeString(band.getName())).append("', ");
        sql.append(band.getCoordinates() != null ? band.getCoordinates().getX() : "NULL").append(", ");
        sql.append(band.getCoordinates() != null ? band.getCoordinates().getY() : "NULL").append(", ");
        
        if (band.getCreationDate() != null) {
            sql.append("'").append(new Timestamp(band.getCreationDate().getTime())).append("', ");
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

    public static String buildUpdate(MusicBand band) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE music_bands SET ");
        sql.append("name = '").append(escapeString(band.getName())).append("', ");
        
        if (band.getCoordinates() != null) {
            sql.append("x = ").append(band.getCoordinates().getX()).append(", ");
            sql.append("y = ").append(band.getCoordinates().getY()).append(", ");
        } else {
            sql.append("x = NULL, y = NULL, ");
        }
        
        if (band.getCreationDate() != null) {
            sql.append("creation_date = '").append(new Timestamp(band.getCreationDate().getTime())).append("', ");
        }
        
        sql.append("number_of_participants = ").append(band.getNumberOfParticipants()).append(", ");
        
        if (band.getDescription() != null) {
            sql.append("description = '").append(escapeString(band.getDescription())).append("', ");
        } else {
            sql.append("description = NULL, ");
        }
        
        if (band.getGenre() != null) {
            sql.append("genre = '").append(band.getGenre().name()).append("', ");
        } else {
            sql.append("genre = NULL, ");
        }
        
        if (band.getBestAlbum() != null) {
            sql.append("album_name = '").append(escapeString(band.getBestAlbum().getName())).append("', ");
            sql.append("album_sales = ").append(band.getBestAlbum().getSales());
        } else {
            sql.append("album_name = NULL, album_sales = NULL");
        }
        
        sql.append(" WHERE id = ").append(band.getId());
        
        return sql.toString();
    }

    public static String buildDelete(long id) {
        return "DELETE FROM music_bands WHERE id = " + id;
    }

    public static String buildSelectAll() {
        return "SELECT id, name, x, y, creation_date, number_of_participants, description, genre, album_name, album_sales FROM music_bands ORDER BY name";
    }

    public static String buildSelectById(long id) {
        return "SELECT * FROM music_bands WHERE id = " + id;
    }

    public static String buildCountByParticipants(int count) {
        return "SELECT COUNT(*) FROM music_bands WHERE number_of_participants = " + count;
    }

    public static String buildAverageParticipants() {
        return "SELECT AVG(number_of_participants) FROM music_bands";
    }

    public static String buildRemoveGreaterThanId(long id) {
        return "DELETE FROM music_bands WHERE id > " + id;
    }

    public static String buildRemoveByBestAlbum(String albumName) {
        return "DELETE FROM music_bands WHERE album_name = '" + escapeString(albumName) + "'";
    }

    public static String buildCountByGenre(MusicGenre genre) {
        return "SELECT COUNT(*) FROM music_bands WHERE genre = '" + genre.name() + "'";
    }

    public static String buildSelectByBestAlbumSales(double sales) {
        return "SELECT * FROM music_bands WHERE album_sales > " + sales;
    }

    private static String escapeString(String value) {
        if (value == null) return "";
        return value.replace("'", "''");
    }
}
