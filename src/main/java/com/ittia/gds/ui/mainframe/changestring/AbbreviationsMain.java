package com.ittia.gds.ui.mainframe.changestring;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays; // Import Arrays

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextArea;

import com.ittia.gds.ui.model.Abbreviation;

public class AbbreviationsMain implements ChangeListener<String> {

    private final Map<String, String> abbreviations;
    private TextArea[] inputAreas; // REMOVED 'final'
    private TextArea outputArea;   // REMOVED 'final'
    private String[] textAreaTitles; // REMOVED 'final'

    private static final Pattern ABBREVIATION_PATTERN = Pattern.compile(":\\s*(\\w+)\\s+");
    private static final String DB_PATH = "jdbc:sqlite:" + System.getProperty("user.dir") + "/src/main/resources/db/abbreviations.db";

    /**
     * Default constructor for AbbreviationsMain.
     * Initializes core components for text expansion.
     */
    public AbbreviationsMain() {
        this.abbreviations = new HashMap<>(); // This can remain final as it's assigned only here
        initializeDatabase();
        initializeAbbreviations();
        // Do NOT initialize inputAreas, outputArea, textAreaTitles here
        // if they are to be assigned in a chained constructor.
        // They will be null until the specific constructor assigns them.
    }

    /**
     * Constructor for AbbreviationsMain used when text input areas are to be monitored
     * for abbreviation expansion.
     * @param inputAreas An array of TextArea elements to monitor for abbreviations.
     * @param outputArea The TextArea where combined output from inputAreas is displayed.
     * @param textAreaTitles The titles corresponding to the inputAreas for display in output.
     */
    public AbbreviationsMain(TextArea[] inputAreas, TextArea outputArea, String[] textAreaTitles) {
        this(); // Call the default constructor to ensure 'abbreviations' map is initialized.

        // Assign the provided input/output areas and titles HERE.
        // These fields are no longer 'final' so they can be assigned after 'this()'.
        this.inputAreas = inputAreas;
        this.outputArea = outputArea;
        this.textAreaTitles = textAreaTitles;
        
        if (this.inputAreas != null && this.inputAreas.length > 0) {
            attachListeners();
        }
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

    private void initializeAbbreviations() {
        abbreviations.clear();
        try (Connection conn = DriverManager.getConnection(DB_PATH);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT key, value FROM abbreviations")) {
            while (rs.next()) {
                abbreviations.put(rs.getString("key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to load abbreviations from database: " + e.getMessage());
        }

        if (abbreviations.isEmpty()) {
            Map<String, String> defaultAbbreviations = new HashMap<>();
            defaultAbbreviations.put(":cc ", "Chief Complaint");
            defaultAbbreviations.put(":pi ", "Present Illness");
            defaultAbbreviations.put(":ros ", "Review of Systems");
            defaultAbbreviations.put(":pmh ", "Past Medical History");
            defaultAbbreviations.put(":s ", "Subjective");
            defaultAbbreviations.put(":o ", "Objective");
            defaultAbbreviations.put(":pe ", "Physical Exam");
            defaultAbbreviations.put(":a ", "Assessment");
            defaultAbbreviations.put(":p ", "Plan");
            defaultAbbreviations.put(":cmt ", "Comment");
            defaultAbbreviations.put(":dx ", "Diagnosis");
            defaultAbbreviations.put(":tx ", "Treatment");
            defaultAbbreviations.put(":rx ", "Prescription");
            defaultAbbreviations.put(":hpi ", "History of Present Illness");
            defaultAbbreviations.put(":fhx ", "Family History");
            defaultAbbreviations.put(":shx ", "Social History");
            defaultAbbreviations.put(":allergies ", "Allergies");
            defaultAbbreviations.put(":meds ", "Medications");
            defaultAbbreviations.put(":vs ", "Vital Signs");
            defaultAbbreviations.put(":cva ", "Cerebrovascular Accident");
            defaultAbbreviations.put(":mi ", "Myocardial Infarction");
            defaultAbbreviations.put(":dm ", "Diabetes Mellitus");
            defaultAbbreviations.put(":htn ", "Hypertension");
            defaultAbbreviations.put(":cad ", "Coronary Artery Disease");

            try (Connection conn = DriverManager.getConnection(DB_PATH);
                 PreparedStatement pstmt = conn.prepareStatement("INSERT OR REPLACE INTO abbreviations (key, value) VALUES (?, ?)")) {
                for (Map.Entry<String, String> entry : defaultAbbreviations.entrySet()) {
                    pstmt.setString(1, entry.getKey());
                    pstmt.setString(2, entry.getValue());
                    pstmt.executeUpdate();
                    abbreviations.put(entry.getKey(), entry.getValue());
                }
            } catch (SQLException e) {
                System.err.println("Failed to insert default abbreviations: " + e.getMessage());
            }
        }
    }

    private void attachListeners() {
        if (inputAreas != null) {
            for (TextArea ta : inputAreas) {
                if (ta != null) {
                    ta.textProperty().addListener(this);
                }
            }
        }
    }

    private void applyAbbreviation(TextArea textArea, String newText) {
        Matcher matcher = ABBREVIATION_PATTERN.matcher(newText);

        if (matcher.find()) {
            String abbreviationKey = ":" + matcher.group(1).toLowerCase() + " ";
            String fullText = abbreviations.get(abbreviationKey);

            if (fullText != null) {
                String replacedText = newText.substring(0, matcher.start()) + fullText + " ";
                
                textArea.textProperty().removeListener(this);
                textArea.setText(replacedText);
                textArea.positionCaret(replacedText.length());
                textArea.textProperty().addListener(this);
            }
        }
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        TextArea sourceTextArea = (TextArea) ((javafx.beans.property.StringProperty) observable).getBean();
        applyAbbreviation(sourceTextArea, newValue);
        updateOutputArea();
    }

    public void addAbbreviation(String key, String value) {
        String formattedKey = key.startsWith(":") ? key : ":" + key;
        formattedKey = formattedKey.endsWith(" ") ? formattedKey : formattedKey + " ";
        
        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement pstmt = conn.prepareStatement("INSERT OR REPLACE INTO abbreviations (key, value) VALUES (?, ?)")) {
            pstmt.setString(1, formattedKey);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
            this.abbreviations.put(formattedKey, value);
        } catch (SQLException e) {
            System.err.println("Failed to add abbreviation to database: " + e.getMessage());
        }
    }

    public void removeAbbreviation(String key) {
        String formattedKey = key.startsWith(":") ? key : ":" + key;
        formattedKey = formattedKey.endsWith(" ") ? formattedKey : formattedKey + " ";
        
        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM abbreviations WHERE key = ?")) {
            pstmt.setString(1, formattedKey);
            pstmt.executeUpdate();
            this.abbreviations.remove(formattedKey);
        } catch (SQLException e) {
            System.err.println("Failed to remove abbreviation from database: " + e.getMessage());
        }
    }

    public Map<String, String> getAllAbbreviations() {
        Map<String, String> allAbbrs = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(DB_PATH);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT key, value FROM abbreviations")) {
            while (rs.next()) {
                allAbbrs.put(rs.getString("key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve all abbreviations from database: " + e.getMessage());
        }
        return allAbbrs;
    }

    public void refreshAbbreviationsMap() {
        abbreviations.clear();
        try (Connection conn = DriverManager.getConnection(DB_PATH);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT key, value FROM abbreviations")) {
            while (rs.next()) {
                abbreviations.put(rs.getString("key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to refresh abbreviations from database: " + e.getMessage());
        }
    }

    private void clearAllInputAreas() {
        if (inputAreas != null && inputAreas.length > 0) {
            for (TextArea ta : inputAreas) {
                if (ta != null) {
                    ta.textProperty().removeListener(this);
                    ta.clear();
                    ta.textProperty().addListener(this);
                }
            }
        }
        updateOutputArea();
    }

    private void updateOutputArea() {
        // Ensure all necessary components are initialized and arrays are of compatible length
        if (outputArea != null && inputAreas != null && textAreaTitles != null && inputAreas.length == textAreaTitles.length) {
            StringBuilder combinedText = new StringBuilder();
            for (int i = 0; i < inputAreas.length; i++) {
                TextArea ta = inputAreas[i];
                if (ta != null && ta.getText() != null && !ta.getText().trim().isEmpty()) {
                    combinedText.append(textAreaTitles[i]).append(" ") // Use the passed titles
                                .append(ta.getText().trim()).append("\n\n");
                }
            }
            outputArea.setText(combinedText.toString().trim());
        }
    }
}