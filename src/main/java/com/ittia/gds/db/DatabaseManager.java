package com.ittia.gds.db;

import com.ittia.gds.ui.model.Abbreviation;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {

    private static final String DB_PATH = "jdbc:sqlite:" + System.getProperty("user.dir") + "/src/main/resources/db/abbreviations.db";

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        File dbDir = new File(System.getProperty("user.dir") + "/src/main/resources/db");
        if (!dbDir.exists()) {
            boolean created = dbDir.mkdirs();
            if (!created) {
                System.err.println("Failed to create directory: " + dbDir.getAbsolutePath());
                return;
            }
        }

        try (Connection conn = DriverManager.getConnection(DB_PATH);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS abbreviations ("
                       + "key TEXT PRIMARY KEY, "
                       + "value TEXT NOT NULL)";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }

    public void addOrUpdateAbbreviation(String key, String value) {
        // Ensure consistent key formatting for DB storage
        String formattedKey = key.startsWith(":") ? key : ":" + key;
        formattedKey = formattedKey.endsWith(" ") ? formattedKey : formattedKey + " ";

        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement pstmt = conn.prepareStatement("INSERT OR REPLACE INTO abbreviations (key, value) VALUES (?, ?)")) {
            pstmt.setString(1, formattedKey);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to add/update abbreviation in database: " + e.getMessage());
        }
    }

    public void deleteAbbreviation(String key) {
        String formattedKey = key.startsWith(":") ? key : ":" + key;
        formattedKey = formattedKey.endsWith(" ") ? formattedKey : formattedKey + " ";

        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM abbreviations WHERE key = ?")) {
            pstmt.setString(1, formattedKey);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to remove abbreviation from database: " + e.getMessage());
        }
    }

    public Map<String, String> getAllAbbreviations() {
        Map<String, String> abbreviations = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(DB_PATH);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT key, value FROM abbreviations")) {
            while (rs.next()) {
                abbreviations.put(rs.getString("key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve all abbreviations from database: " + e.getMessage());
        }
        return abbreviations;
    }
}