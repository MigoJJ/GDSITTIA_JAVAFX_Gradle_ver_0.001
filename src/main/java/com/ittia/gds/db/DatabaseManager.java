package com.ittia.gds.db;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    // Path relative to the resources folder
	private static final String DB_PATH = System.getProperty("user.dir") + "/src/main/resources/db/abbreviations.db";
	private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    public DatabaseManager() {
        ensureDbDirectoryExists();  // Create directory if it doesn't exist
        createTableIfNotExists();
    }

    private void ensureDbDirectoryExists() {
        File dbDir = new File("src/main/resources/db");
        if (!dbDir.exists()) {
            dbDir.mkdirs();  // Create all necessary parent directories
        }
    }
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS abbreviations (
                key TEXT PRIMARY KEY NOT NULL,
                value TEXT NOT NULL
            );""";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Database error on table creation: " + e.getMessage());
        }
    }

    public Map<String, String> getAllAbbreviations() {
        Map<String, String> abbreviations = new HashMap<>();
        String sql = "SELECT key, value FROM abbreviations";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                abbreviations.put(rs.getString("key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching all abbreviations: " + e.getMessage());
        }
        return abbreviations;
    }

    public void addOrUpdateAbbreviation(String key, String value) {
        // "INSERT OR REPLACE" is an SQLite-specific command that simplifies add/update logic.
        String sql = "INSERT OR REPLACE INTO abbreviations (key, value) VALUES (?, ?)";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key.toLowerCase());
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error adding/updating abbreviation: " + e.getMessage());
        }
    }

    public void deleteAbbreviation(String key) {
        String sql = "DELETE FROM abbreviations WHERE key = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key.toLowerCase());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error deleting abbreviation: " + e.getMessage());
        }
    }
}