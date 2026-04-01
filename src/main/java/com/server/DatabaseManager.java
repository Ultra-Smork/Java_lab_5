package com.server;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseManager {
    private static final String DEFAULT_URL = "postgresql://s410022:kGwQIW2srjmKk48W@127.0.0.1:5432/studs";
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

    private static String buildJdbcUrl(String postgresqlUrl) {
        // Format: postgresql://username:password@host:port/database
        int atIndex = postgresqlUrl.indexOf('@');
        String afterAt = postgresqlUrl.substring(atIndex + 1);
        
        int colonIndex = afterAt.indexOf(':');
        int slashIndex = afterAt.indexOf('/');
        
        String hostPort = afterAt.substring(0, colonIndex);
        String database = afterAt.substring(slashIndex + 1);
        
        return "jdbc:postgresql://" + hostPort + "/" + database;
    }

    private static String[] extractCredentials(String postgresqlUrl) {
        // Format: postgresql://username:password@host:port/database
        int atIndex = postgresqlUrl.indexOf('@');
        String beforeAt = postgresqlUrl.substring("postgresql://".length(), atIndex);
        String[] parts = beforeAt.split(":");
        
        String user = parts[0];
        String password = parts[1];
        return new String[]{user, password};
    }

    public static void initialize() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC driver not found");
        }
        runMigrations();
    }

    private static void runMigrations() throws SQLException {
        String postgresqlUrl = getDatabaseUrl();
        String jdbcUrl = buildJdbcUrl(postgresqlUrl);
        String[] creds = extractCredentials(postgresqlUrl);

        try (Connection conn = DriverManager.getConnection(jdbcUrl, creds[0], creds[1]);
             Statement stmt = conn.createStatement()) {

            List<String> migrationFiles = Arrays.asList(
                "V1__initial_schema.sql",
                "V2__create_genre_table.sql",
                "V3__command_history.sql"
            );

            for (String migrationFile : migrationFiles) {
                try {
                    System.out.println("Running migration: " + migrationFile);
                    java.io.InputStream is = DatabaseManager.class.getClassLoader().getResourceAsStream(migrationFile);
                    if (is == null) {
                        System.out.println("Migration file not found in classpath: " + migrationFile);
                        continue;
                    }
                    String sql = new String(is.readAllBytes());
                    is.close();
                    String[] statements = sql.split(";");
                    for (String statement : statements) {
                        String trimmed = statement.trim();
                        if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                            stmt.execute(trimmed);
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
        String postgresqlUrl = getDatabaseUrl();
        String jdbcUrl = buildJdbcUrl(postgresqlUrl);
        String[] creds = extractCredentials(postgresqlUrl);
        
        try (Connection conn = DriverManager.getConnection(jdbcUrl, creds[0], creds[1]);
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

    public static ResultSet executeQuery(String sql) throws SQLException {
        String postgresqlUrl = getDatabaseUrl();
        String jdbcUrl = buildJdbcUrl(postgresqlUrl);
        String[] creds = extractCredentials(postgresqlUrl);
        
        Connection conn = DriverManager.getConnection(jdbcUrl, creds[0], creds[1]);
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    public static int executeUpdate(String sql) throws SQLException {
        String postgresqlUrl = getDatabaseUrl();
        String jdbcUrl = buildJdbcUrl(postgresqlUrl);
        String[] creds = extractCredentials(postgresqlUrl);
        
        try (Connection conn = DriverManager.getConnection(jdbcUrl, creds[0], creds[1]);
             Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    public static long executeInsert(String sql) throws SQLException {
        String postgresqlUrl = getDatabaseUrl();
        String jdbcUrl = buildJdbcUrl(postgresqlUrl);
        String[] creds = extractCredentials(postgresqlUrl);
        
        try (Connection conn = DriverManager.getConnection(jdbcUrl, creds[0], creds[1]);
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
        String postgresqlUrl = getDatabaseUrl();
        String jdbcUrl = buildJdbcUrl(postgresqlUrl);
        String[] creds = extractCredentials(postgresqlUrl);
        
        String sql = "INSERT INTO command_history (session_id, command) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, creds[0], creds[1]);
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
        String postgresqlUrl = getDatabaseUrl();
        String jdbcUrl = buildJdbcUrl(postgresqlUrl);
        String[] creds = extractCredentials(postgresqlUrl);
        
        String sql = "SELECT command FROM command_history WHERE session_id = ? ORDER BY executed_at DESC LIMIT 11";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, creds[0], creds[1]);
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
        String postgresqlUrl = getDatabaseUrl();
        String jdbcUrl = buildJdbcUrl(postgresqlUrl);
        String[] creds = extractCredentials(postgresqlUrl);
        
        String sql = "SELECT COUNT(*) FROM music_bands";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, creds[0], creds[1]);
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
}
