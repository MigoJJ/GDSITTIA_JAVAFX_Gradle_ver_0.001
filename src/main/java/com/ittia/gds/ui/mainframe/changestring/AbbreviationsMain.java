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

public class AbbreviationsMain implements ChangeListener<String> {

    public void showManagerUI() {
        Stage managerStage = new Stage();
        managerStage.setTitle("Manage Abbreviations");
        managerStage.setMinWidth(400);   // Added for consistent sizing
        managerStage.setMinHeight(400);  // Added for consistent sizing
        
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        
        TextArea abbreviationsDisplay = new TextArea();
        abbreviationsDisplay.setEditable(false);
        abbreviationsDisplay.setPrefHeight(200);
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : abbreviations.entrySet()) {
            sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }
        abbreviationsDisplay.setText(sb.toString());
        
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        
        TextField abbreviationField = new TextField();
        TextField expansionField = new TextField();
        Button addButton = new Button("Add/Update");
        Button deleteButton = new Button("Delete");
        Button findButton = new Button("Find");
        Button quitButton = new Button("Quit");
        // Add the new Clear button
        Button clearInputAreasButton = new Button("Clear Input Areas"); 
        
        form.add(new Label("Abbreviation (e.g., :cc ):"), 0, 0);
        form.add(abbreviationField, 1, 0);
        form.add(new Label("Expansion:"), 0, 1);
        form.add(expansionField, 1, 1);
        form.add(addButton, 0, 2);
        form.add(deleteButton, 1, 2);
        form.add(findButton, 2, 2);
        form.add(quitButton, 3, 2);
        // Add the Clear button to the grid, possibly on a new row or extending the current one
        form.add(clearInputAreasButton, 4, 2); // Placed next to Quit button
        
        addButton.setOnAction(e -> {
            String inputKey = abbreviationField.getText().trim();
            if (!inputKey.isEmpty() && !expansionField.getText().isEmpty()) {
                String formattedKey = inputKey.startsWith(":") ? inputKey : ":" + inputKey;
                formattedKey = formattedKey.endsWith(" ") ? formattedKey : formattedKey + " ";
                addAbbreviation(formattedKey, expansionField.getText());
                // After adding/updating, refresh the display and clear input fields
                refreshAbbreviationDisplay(abbreviationsDisplay, sb);
                abbreviationField.clear();
                expansionField.clear();
            }
        });
        
        deleteButton.setOnAction(e -> {
            String inputKey = abbreviationField.getText().trim();
            if (!inputKey.isEmpty()) {
                String formattedKey = inputKey.startsWith(":") ? inputKey : ":" + inputKey;
                formattedKey = formattedKey.endsWith(" ") ? formattedKey : formattedKey + " ";
                removeAbbreviation(formattedKey);
                // After deleting, refresh the display and clear input fields
                refreshAbbreviationDisplay(abbreviationsDisplay, sb);
                abbreviationField.clear();
                expansionField.clear();
            }
        });
        
        findButton.setOnAction(e -> {
            String inputKey = abbreviationField.getText().trim();
            if (!inputKey.isEmpty()) {
                String formattedKey = inputKey.startsWith(":") ? inputKey : ":" + inputKey;
                formattedKey = formattedKey.endsWith(" ") ? formattedKey : formattedKey + " ";
                String expansion = abbreviations.get(formattedKey);
                expansionField.setText(expansion != null ? expansion : "Not found");
            }
        });
        
        quitButton.setOnAction(e -> {
            managerStage.close();
        });

        // Action for the new Clear button
        clearInputAreasButton.setOnAction(e -> {
              // Additionally clear the fields in the manager UI
                abbreviationField.clear(); // <--- Added this line
                expansionField.clear();    // <--- Added this line
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

    private final Map<String, String> abbreviations;

    private final TextArea[] inputAreas;

    private final TextArea outputArea;

    private static final Pattern ABBREVIATION_PATTERN = Pattern.compile(":\\s*(\\w+)\\s+");

    private static final String DB_PATH = "jdbc:sqlite:" + System.getProperty("user.dir") + "/src/main/resources/db/abbreviations.db";

    public AbbreviationsMain(TextArea[] inputAreas, TextArea outputArea) {
        this.inputAreas = inputAreas;
        this.outputArea = outputArea;
        this.abbreviations = new HashMap<>();
        initializeDatabase();
        initializeAbbreviations();
        attachListeners();
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
        for (TextArea ta : inputAreas) {
            if (ta != null) {
                ta.textProperty().addListener(this);
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
        // When any input area changes, update the output area.
        // This is important for the combined display to be accurate.
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
            abbreviations.put(formattedKey, value);
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
            abbreviations.remove(formattedKey);
        } catch (SQLException e) {
            System.err.println("Failed to remove abbreviation from database: " + e.getMessage());
        }
    }

    public Map<String, String> getAbbreviations() {
        return java.util.Collections.unmodifiableMap(abbreviations);
    }

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

    /**
     * Clears the text from all input TextAreas.
     * It temporarily removes listeners to prevent abbreviation processing during clearing.
     */
    private void clearAllInputAreas() {
        for (TextArea ta : inputAreas) {
            if (ta != null) {
                // Temporarily remove listener to avoid triggering changes during clear
                ta.textProperty().removeListener(this); 
                ta.clear(); // Clear the text
                ta.textProperty().addListener(this); // Re-add the listener
            }
        }
        updateOutputArea(); // Clear the output area as well, or update it to reflect empty inputs
    }

    /**
     * Helper method to refresh the abbreviations display TextArea.
     */
    private void refreshAbbreviationDisplay(TextArea displayArea, StringBuilder sb) {
        sb.setLength(0); // Clear existing content
        for (Map.Entry<String, String> entry : abbreviations.entrySet()) {
            sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }
        displayArea.setText(sb.toString());
    }

    /**
     * Updates the content of the outputArea by concatenating text from all inputAreas.
     */
    private void updateOutputArea() {
        if (outputArea != null) {
            StringBuilder combinedText = new StringBuilder();
            for (TextArea ta : inputAreas) {
                if (ta != null && ta.getText() != null) {
                    combinedText.append(ta.getText()).append("\n"); // Append text and a newline
                }
            }
            // Temporarily remove listener from outputArea if it has one and this class monitors it.
            // (Assuming GDSEMR_Abbreviations only listens to inputAreas, this might not be strictly necessary here,
            // but is good practice if outputArea itself could trigger unintended cascades.)
            outputArea.setText(combinedText.toString().trim()); // .trim() to remove trailing newline if only one input
        }
    }
}