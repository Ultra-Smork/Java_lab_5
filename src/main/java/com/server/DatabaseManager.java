package com.server;

import com.model.MusicBand;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseManager {
    private static final String DEFAULT_URL = "postgresql://s410022:kGwQIW2srjmKk48W@127.0.0.1:5432/studs";
    private static String databaseUrl;
    private static DatabaseManager instance;
    private static boolean embeddedMode = false;
    private static final String EMBEDDED_JDBC_URL = "jdbc:h2:file:./data/localdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";

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

    public static void setEmbeddedMode(boolean enabled) {
        embeddedMode = enabled;
    }

    public static boolean isEmbeddedMode() {
        return embeddedMode;
    }

    private static Connection getConnection() throws SQLException {
        if (embeddedMode) {
            return DriverManager.getConnection(EMBEDDED_JDBC_URL);
        }
        String url = getDatabaseUrl();
        String jdbcUrl = buildJdbcUrl(url);
        String[] creds = extractCredentials(url);
        return DriverManager.getConnection(jdbcUrl, creds[0], creds[1]);
    }

    private static String buildJdbcUrl(String postgresqlUrl) {
        int atIndex = postgresqlUrl.indexOf('@');
        String afterAt = postgresqlUrl.substring(atIndex + 1);

        int colonIndex = afterAt.indexOf(':');
        int slashIndex = afterAt.indexOf('/');

        String hostPort = afterAt.substring(0, colonIndex);
        String database = afterAt.substring(slashIndex + 1);

        return "jdbc:postgresql://" + hostPort + "/" + database;
    }

    private static String[] extractCredentials(String postgresqlUrl) {
        int atIndex = postgresqlUrl.indexOf('@');
        String beforeAt = postgresqlUrl.substring("postgresql://".length(), atIndex);
        String[] parts = beforeAt.split(":");

        String user = parts[0];
        String password = parts[1];
        return new String[]{user, password};
    }

    public static void initialize() throws SQLException {
        try {
            if (embeddedMode) {
                Class.forName("org.h2.Driver");
            } else {
                Class.forName("org.postgresql.Driver");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Database driver not found: " + e.getMessage());
        }
        runMigrations();
    }

    private static void runMigrations() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String suffix = embeddedMode ? "_h2" : "";
            List<String> migrationFiles = Arrays.asList(
                "V1__initial_schema" + suffix + ".sql",
                "V2__create_genre_table" + suffix + ".sql",
                "V3__command_history" + suffix + ".sql",
                "V4__users_table" + suffix + ".sql"
            );

            for (String migrationFile : migrationFiles) {
                try {
                    System.out.println("Running migration: " + migrationFile);
                    InputStream is = DatabaseManager.class.getClassLoader().getResourceAsStream(migrationFile);
                    if (is == null) {
                        System.out.println("Migration file not found in classpath: " + migrationFile);
                        if (suffix.isEmpty()) {
                            System.out.println("Skipping missing migration: " + migrationFile);
                            continue;
                        }
                        String fallback = migrationFile.replace("_h2.sql", ".sql");
                        System.out.println("Trying fallback: " + fallback);
                        is = DatabaseManager.class.getClassLoader().getResourceAsStream(fallback);
                        if (is == null) {
                            System.out.println("Fallback not found either, skipping");
                            continue;
                        }
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
