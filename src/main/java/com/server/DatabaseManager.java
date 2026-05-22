package com.server;

import com.model.MusicBand;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseManager {
    private static final String DEFAULT_URL = "postgresql://localhost:5432/postgres?user=postgres";
    private static String databaseUrl;
    private static DatabaseManager instance;

    private DatabaseManager() {
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public static void setDatabaseUrl(String url) {
        databaseUrl = url;
    }

    public static String getDatabaseUrl() {
        if (databaseUrl == null) {
            databaseUrl = DEFAULT_URL;
        }
        return databaseUrl;
    }

    private static Connection getConnection() throws SQLException {
        String url = getDatabaseUrl();
        if (url.startsWith("postgresql://")) {
            return DriverManager.getConnection(zbuildJdbcUrl(url));
        }
        return DriverManager.getConnection(url);
    }

    private static String buildJdbcUrl(String pgUrl) {
        return "jdbc:" + pgUrl;
    }

    public static void initialize() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL driver not found: " + e.getMessage());
        }
        runMigrations();
    }

    private static void runMigrations() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            List<String> migrationFiles = Arrays.asList(
                "V1__initial_schema.sql",
                "V2__create_genre_table.sql",
                "V3__command_history.sql",
                "V4__users_table.sql"
            );

            for (String migrationFile : migrationFiles) {
                try {
                    System.out.println("Running migration: " + migrationFile);
                    InputStream is = DatabaseManager.class.getClassLoader().getResourceAsStream(migrationFile);
                    if (is == null) {
                        System.out.println("Migration file not found in classpath: " + migrationFile);
                        continue;
                    }
                    String sql = new String(is.readAllBytes());
                    is.close();
                    StringBuilder cleanSql = new StringBuilder();
                    for (String line : sql.split("\n")) {
                        line = line.trim();
                        if (line.startsWith("--") || line.isEmpty()) continue;
                        cleanSql.append(line).append(" ");
                    }
                    String[] statements = cleanSql.toString().split(";");
                    for (String statement : statements) {
                        String trimmed = statement.trim();
                        if (!trimmed.isEmpty() && trimmed.length() > 5) {
                            try {
                                stmt.execute(trimmed);
                            } catch (SQLException e) {
                                String msg = e.getMessage().toLowerCase();
                                if (msg.contains("already exists") ||
                                    msg.contains("duplicate") ||
                                    msg.contains("unique constraint")) {
                                    // Ignore idempotent errors
                                } else {
                                    System.out.println("Warning: " + e.getMessage());
                                }
                            }
                        }
                    }
                    System.out.println("Migration completed: " + migrationFile);
                } catch (IOException e) {
                    System.out.println("Failed to read migration file: " + migrationFile + " - " + e.getMessage());
                }
            }
        }
    }

    public static String executeQueryToString(String sql) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            StringBuilder sb = new StringBuilder();
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    sb.append(rs.getMetaData().getColumnName(i)).append("=").append(rs.getString(i));
                    if (i < columnCount) sb.append("|");
                }
                sb.append(";");
            }
            return sb.toString();
        }
    }

    public static List<MusicBand> executeQueryBands(String sql) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<MusicBand> bands = new ArrayList<>();
            while (rs.next()) {
                MusicBand band = new MusicBand();
                band.setId(rs.getLong("id"));
                band.setName(rs.getString("name"));

                long x = rs.getLong("x");
                int y = rs.getInt("y");
                if (!rs.wasNull()) {
                    band.setCoordinates(new com.model.Coordinates(x, y));
                }

                Timestamp ts = rs.getTimestamp("creation_date");
                if (ts != null) {
                    band.setCreationDate(new java.util.Date(ts.getTime()));
                }
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

                bands.add(band);
            }
            return bands;
        }
    }

    public static int executeUpdate(String sql) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    public static long executeInsert(String sql) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return -1;
        }
    }

    public static void saveCommand(String command, String sessionId) {
        String sql = "INSERT INTO command_history (session_id, command) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            pstmt.setString(2, command);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save command history: " + e.getMessage());
        }
    }

    public static List<String> getCommandHistory(String sessionId) {
        List<String> history = new ArrayList<>();
        String sql = "SELECT command FROM command_history WHERE session_id = ? ORDER BY executed_at DESC LIMIT 11";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                history.add(rs.getString("command"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get command history: " + e.getMessage());
        }
        return history;
    }

    public static int getBandCount() {
        String sql = "SELECT COUNT(*) FROM music_bands";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get band count: " + e.getMessage());
        }
        return 0;
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean registerUser(String login, String passwordHash) {
        String sql = "INSERT INTO users (login, password_hash) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, login);
            pstmt.setString(2, passwordHash);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to register user: " + e.getMessage());
            return false;
        }
    }

    public static boolean validateUser(String login, String passwordHash) {
        String sql = "SELECT password_hash FROM users WHERE login = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, login);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                return storedHash.equals(passwordHash);
            }
        } catch (SQLException e) {
            System.err.println("Failed to validate user: " + e.getMessage());
        }
        return false;
    }

    public static boolean userExists(String login) {
        String sql = "SELECT login FROM users WHERE login = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, login);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Failed to check user exists: " + e.getMessage());
        }
        return false;
    }
}
