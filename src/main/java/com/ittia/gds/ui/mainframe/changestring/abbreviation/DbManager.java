package com.ittia.gds.ui.mainframe.changestring.abbreviation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DbManager {

    private static final String DB_NAME = "/ui/mainframe/changestring/abbriviation/abbreviations.db"; // Changed path for DB
    private static final String TABLE_NAME = "abbreviations";
    private static String DB_URL; // Will be initialized with EntryDir.dbDir

    public DbManager(Path dbdir) {
        // Ensure EntryDir.dbDir is correctly configured and available
        DB_URL = "jdbc:sqlite:" + dbdir + DB_NAME;
        createTable();
    }

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
        }
        return conn;
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (\n"
                + "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    abbreviation TEXT NOT NULL UNIQUE,\n"
                + "    full_text TEXT NOT NULL\n"
                + ");";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Abbreviations table created or already exists.");
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }

    public void insertAbbreviation(String abbreviation, String fullText) {
        String sql = "INSERT OR IGNORE INTO " + TABLE_NAME + "(abbreviation, full_text) VALUES(?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, abbreviation);
            pstmt.setString(2, fullText);
            pstmt.executeUpdate();
            // System.out.println("Abbreviation inserted: " + abbreviation); // For debugging
        } catch (SQLException e) {
            System.err.println("Error inserting abbreviation: " + e.getMessage());
        }
    }

    public Map<String, String> getAllAbbreviations() {
        Map<String, String> abbreviations = new HashMap<>();
        String sql = "SELECT abbreviation, full_text FROM " + TABLE_NAME;
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                abbreviations.put(rs.getString("abbreviation"), rs.getString("full_text"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving abbreviations: " + e.getMessage());
        }
        return abbreviations;
    }

    public void updateAbbreviation(String abbreviation, String newFullText) {
        String sql = "UPDATE " + TABLE_NAME + " SET full_text = ? WHERE abbreviation = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newFullText);
            pstmt.setString(2, abbreviation);
            pstmt.executeUpdate();
            System.out.println("Abbreviation updated: " + abbreviation);
        } catch (SQLException e) {
            System.err.println("Error updating abbreviation: " + e.getMessage());
        }
    }

    public void deleteAbbreviation(String abbreviation) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE abbreviation = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, abbreviation);
            pstmt.executeUpdate();
            System.out.println("Abbreviation deleted: " + abbreviation);
        } catch (SQLException e) {
            System.err.println("Error deleting abbreviation: " + e.getMessage());
        }
    }

    /**
     * This method can be used to initially populate the database from the old text file.
     * Call this once after setting up the new DbManager.
     * @param oldFilePath The path to your old 'extracteddata.txt' file.
     */
    public void importFromOldFile(String oldFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(oldFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("replacements.put(")) {
                    String[] parts = line.split("\"");
                    if (parts.length >= 4) {
                        String abbr = parts[1];
                        String full = parts[3];
                        insertAbbreviation(abbr, full);
                    }
                }
            }
            System.out.println("Abbreviations imported from old file.");
        } catch (IOException e) {
            System.err.println("Error importing from old file: " + e.getMessage());
        }
    }
}