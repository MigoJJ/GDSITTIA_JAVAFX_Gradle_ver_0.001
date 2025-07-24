package com.ittia.gds.ui.model;

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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * GDSEMR_Abbreviations class provides functionality to manage and apply text abbreviations
 * within JavaFX TextArea components. It listens for text changes in specified input
 * TextAreas and replaces predefined abbreviation patterns (e.g., ":cc ") with their
 * full forms.
 */
public class GDSEMR_Abbreviations implements ChangeListener<String> {

    /**
     * Shows a UI dialog for managing abbreviations (adding, removing, editing, finding, and quitting)
     */
    public void showManagerUI() {
        Stage managerStage = new Stage();
        managerStage.setTitle("Manage Abbreviations");
        
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        
        TextArea abbreviationsDisplay = new TextArea();
        abbreviationsDisplay.setEditable(false);
        abbreviationsDisplay.setPrefHeight(200);
        
        // Display current abbreviations
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : abbreviations.entrySet()) {
            sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }
        abbreviationsDisplay.setText(sb.toString());
        
        // Form for managing abbreviations
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        
        TextField abbreviationField = new TextField();
        TextField expansionField = new TextField();
        Button addButton = new Button("Add/Update");
        Button deleteButton = new Button("Delete");
        Button findButton = new Button("Find");
        Button quitButton = new Button("Quit");
        
        form.add(new Label("Abbreviation (e.g., :cc ):"), 0, 0);
        form.add(abbreviationField, 1, 0);
        form.add(new Label("Expansion:"), 0, 1);
        form.add(expansionField, 1, 1);
        form.add(addButton, 0, 2);
        form.add(deleteButton, 1, 2);
        form.add(findButton, 2, 2);
        form.add(quitButton, 3, 2);
        
        // Add/Update button action
        addButton.setOnAction(e -> {
            String inputKey = abbreviationField.getText().trim();
            if (!inputKey.isEmpty() && !expansionField.getText().isEmpty()) {
                // Ensure the key starts with ":" and ends with " "
                String formattedKey = inputKey.startsWith(":") ? inputKey : ":" + inputKey;
                formattedKey = formattedKey.endsWith(" ") ? formattedKey : formattedKey + " ";
                addAbbreviation(formattedKey, expansionField.getText());
                // Refresh display
                sb.setLength(0);
                for (Map.Entry<String, String> entry : abbreviations.entrySet()) {
                    sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
                }
                abbreviationsDisplay.setText(sb.toString());
                abbreviationField.clear();
                expansionField.clear();
            }
        });
        
        // Delete button action
        deleteButton.setOnAction(e -> {
            String inputKey = abbreviationField.getText().trim();
            if (!inputKey.isEmpty()) {
                // Ensure the key starts with ":" and ends with " "
                String formattedKey = inputKey.startsWith(":") ? inputKey : ":" + inputKey;
                formattedKey = formattedKey.endsWith(" ") ? formattedKey : formattedKey + " ";
                removeAbbreviation(formattedKey);
                // Refresh display
                sb.setLength(0);
                for (Map.Entry<String, String> entry : abbreviations.entrySet()) {
                    sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
                }
                abbreviationsDisplay.setText(sb.toString());
                abbreviationField.clear();
                expansionField.clear();
            }
        });
        
        // Find button action
        findButton.setOnAction(e -> {
            String inputKey = abbreviationField.getText().trim();
            if (!inputKey.isEmpty()) {
                // Ensure the key starts with ":" and ends with " "
                String formattedKey = inputKey.startsWith(":") ? inputKey : ":" + inputKey;
                formattedKey = formattedKey.endsWith(" ") ? formattedKey : formattedKey + " ";
                String expansion = abbreviations.get(formattedKey);
                expansionField.setText(expansion != null ? expansion : "Not found");
            }
        });
        
        // Quit button action
        quitButton.setOnAction(e -> {
            managerStage.close();
        });
        
        layout.getChildren().addAll(
            new Label("Current Abbreviations:"),
            abbreviationsDisplay,
            new Label("Manage Abbreviations:"),
            form
        );
        
        Scene scene = new Scene(layout, 400, 400);
        managerStage.setScene(scene);
        managerStage.show();
    }

    // --- Fields ---

    /**
     * A map to store abbreviations. Key: abbreviation trigger (e.g., ":cc "), Value: full text (e.g., "Chief Complaint").
     */
    private final Map<String, String> abbreviations;

    /**
     * Reference to the array of input TextAreas that this listener will monitor.
     */
    private final TextArea[] inputAreas;

    /**
     * Reference to the output TextArea where the combined and processed text is displayed.
     * This is needed to ensure the output updates correctly after an abbreviation is applied.
     */
    private final TextArea outputArea;

    /**
     * A regular expression pattern to detect the abbreviation trigger.
     * Example: ":word " where 'word' is the abbreviation key.
     * The pattern `:\\s*(\\w+)\\s+` matches a colon, followed by optional whitespace,
     * then one or more word characters (captured in group 1), followed by one or more whitespace characters.
     * This ensures the abbreviation is detected when typed with a space at the end.
     */
    private static final Pattern ABBREVIATION_PATTERN = Pattern.compile(":\\s*(\\w+)\\s+");

    /**
     * Path to the SQLite database, using user.dir for the project root.
     */
    private static final String DB_PATH = "jdbc:sqlite:" + System.getProperty("user.dir") + "/src/main/resources/db/abbreviations.db";

    // --- Constructor ---

    /**
     * Constructs a new GDSEMR_Abbreviations instance.
     *
     * @param inputAreas An array of TextArea components to monitor for abbreviation triggers.
     * @param outputArea The TextArea where the combined and processed output is displayed.
     */
    public GDSEMR_Abbreviations(TextArea[] inputAreas, TextArea outputArea) {
        this.inputAreas = inputAreas;
        this.outputArea = outputArea;
        this.abbreviations = new HashMap<>();
        initializeDatabase(); // Initialize the database and table
        initializeAbbreviations(); // Populate the initial set of abbreviations from the database
        attachListeners(); // Attach this ChangeListener to all input TextAreas
    }

    // --- Private Helper Methods ---

    /**
     * Initializes the SQLite database and creates the abbreviations table if it doesn't exist.
     */
    private void initializeDatabase() {
        // Ensure the db directory exists
        File dbDir = new File(System.getProperty("user.dir") + "/src/main/resources/db");
        if (!dbDir.exists()) {
            boolean created = dbDir.mkdirs();
            if (!created) {
                System.err.println("Failed to create directory: " + dbDir.getAbsolutePath());
                return;
            }
        }

        // Initialize the database and create the table
        try (Connection conn = DriverManager.getConnection(DB_PATH);
             Statement stmt = conn.createStatement()) {
            // Create table if it doesn't exist
            String sql = "CREATE TABLE IF NOT EXISTS abbreviations ("
                       + "key TEXT PRIMARY KEY, "
                       + "value TEXT NOT NULL)";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }

    /**
     * Initializes the map by loading abbreviations from the SQLite database.
     * If the database is empty, it populates it with default abbreviations.
     */
    private void initializeAbbreviations() {
        // Load existing abbreviations from the database
        try (Connection conn = DriverManager.getConnection(DB_PATH);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT key, value FROM abbreviations")) {
            while (rs.next()) {
                abbreviations.put(rs.getString("key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to load abbreviations from database: " + e.getMessage());
        }

        // If the database is empty, populate it with default abbreviations
        if (abbreviations.isEmpty()) {
            // Default abbreviations
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

            // Insert default abbreviations into the database
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

    /**
     * Attaches this ChangeListener instance to the textProperty of all
     * specified input TextAreas. This ensures that the `changed` method
     * is invoked whenever the text in any of these TextAreas is modified.
     */
    private void attachListeners() {
        for (TextArea ta : inputAreas) {
            if (ta != null) {
                ta.textProperty().addListener(this);
            }
        }
    }

    /**
     * Applies the abbreviation logic to the given TextArea's text.
     * This method is called when a change is detected in an input TextArea.
     * It checks if the new text ends with a recognized abbreviation pattern
     * and, if so, replaces it with the full text.
     *
     * @param textArea The TextArea whose text has changed and needs abbreviation processing.
     * @param newText The new text content of the TextArea after the change.
     */
    private void applyAbbreviation(TextArea textArea, String newText) {
        // Check if the new text ends with the abbreviation pattern ":word "
        Matcher matcher = ABBREVIATION_PATTERN.matcher(newText);

        // If the pattern is found
        if (matcher.find()) {
            String abbreviationKey = ":" + matcher.group(1).toLowerCase() + " "; // Reconstruct the full key (e.g., ":cc ")
            String fullText = abbreviations.get(abbreviationKey); // Look up the full text in the abbreviations map

            // If a corresponding full text is found for the abbreviation key
            if (fullText != null) {
                // Construct the replaced text:
                // Take the part of the string before the abbreviation trigger,
                // append the full text, and then add a space for readability.
                String replacedText = newText.substring(0, matcher.start()) + fullText + " ";
                
                // Temporarily remove the listener to prevent infinite loop when setting text
                textArea.textProperty().removeListener(this);
                textArea.setText(replacedText); // Update the TextArea with the expanded text
                textArea.positionCaret(replacedText.length()); // Move the caret to the end of the new text
                // Re-add the listener
                textArea.textProperty().addListener(this);
            }
        }
    }

    // --- Public Methods (ChangeListener Interface Implementation) ---

    /**
     * Called when the text property of an observed TextArea changes.
     * This is the core method where abbreviation detection and replacement occurs.
     * It casts the observable to the source TextArea and then applies the
     * abbreviation logic.
     *
     * @param observable The ObservableValue (textProperty of a TextArea) that changed.
     * @param oldValue The old string value of the TextArea's text.
     * @param newValue The new string value of the TextArea's text.
     */
    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        // Cast the observable back to TextArea to get the source of the change.
        // This cast is safe because we only attach this listener to TextArea textProperties.
        TextArea sourceTextArea = (TextArea) ((javafx.beans.property.StringProperty) observable).getBean();

        // Apply abbreviation logic to the current TextArea.
        applyAbbreviation(sourceTextArea, newValue);
    }

    // --- Optional Public Methods (for managing abbreviations dynamically) ---

    /**
     * Adds a new abbreviation or updates an existing one in both the map and the database.
     * The key is formatted to include a colon and trailing space.
     * @param key The abbreviation trigger (e.g., ":dx ").
     * @param value The full text (e.g., "Diagnosis").
     */
    public void addAbbreviation(String key, String value) {
        // Ensure the key starts with ":" and ends with " "
        String formattedKey = key.startsWith(":") ? key : ":" + key;
        formattedKey = formattedKey.endsWith(" ") ? formattedKey : formattedKey + " ";
        
        // Update the database
        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement pstmt = conn.prepareStatement("INSERT OR REPLACE INTO abbreviations (key, value) VALUES (?, ?)")) {
            pstmt.setString(1, formattedKey);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
            abbreviations.put(formattedKey, value);
        } catch (SQLException e) {
            System.err.println("Failed to add abbreviation to database: " + e.getMessage());
        }
    }

    /**
     * Removes an abbreviation from both the map and the database.
     * The key is formatted to include a colon and trailing space.
     * @param key The abbreviation trigger to remove.
     */
    public void removeAbbreviation(String key) {
        String formattedKey = key.startsWith(":") ? key : ":" + key;
        formattedKey = formattedKey.endsWith(" ") ? formattedKey : formattedKey + " ";
        
        // Remove from the database
        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM abbreviations WHERE key = ?")) {
            pstmt.setString(1, formattedKey);
            pstmt.executeUpdate();
            abbreviations.remove(formattedKey);
        } catch (SQLException e) {
            System.err.println("Failed to remove abbreviation from database: " + e.getMessage());
        }
    }

    /**
     * Retrieves the current map of abbreviations.
     * @return An unmodifiable map of abbreviations.
     */
    public Map<String, String> getAbbreviations() {
        return java.util.Collections.unmodifiableMap(abbreviations);
    }

    /**
     * Refreshes the abbreviations map by reloading from the database.
     */
    public void refreshAbbreviations() {
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
}