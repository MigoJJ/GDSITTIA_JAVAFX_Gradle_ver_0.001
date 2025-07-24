package com.ittia.gds.ui.model;

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
	 * Shows a UI dialog for managing abbreviations (adding, removing, editing)
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
	    
	    // Form for adding new abbreviations
	    GridPane form = new GridPane();
	    form.setHgap(10);
	    form.setVgap(10);
	    
	    TextField abbreviationField = new TextField();
	    TextField expansionField = new TextField();
	    Button addButton = new Button("Add/Update");
	    
	    form.add(new Label("Abbreviation:"), 0, 0);
	    form.add(abbreviationField, 1, 0);
	    form.add(new Label("Expansion:"), 0, 1);
	    form.add(expansionField, 1, 1);
	    form.add(addButton, 1, 2);
	    
	    addButton.setOnAction(e -> {
	        if (!abbreviationField.getText().isEmpty() && !expansionField.getText().isEmpty()) {
	            addAbbreviation(abbreviationField.getText(), expansionField.getText());
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
	    
	    layout.getChildren().addAll(
	        new Label("Current Abbreviations:"),
	        abbreviationsDisplay,
	        new Label("Add/Edit Abbreviations:"),
	        form
	    );
	    
	    Scene scene = new Scene(layout, 400, 400);
	    managerStage.setScene(scene);
	    managerStage.show();
	}
	// --- Fields ---
	

    /**
     * A map to store abbreviations. Key: abbreviation trigger (e.g., "cc"), Value: full text (e.g., "Chief Complaint").
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
     * The pattern should capture the word after the colon.
     * The pattern `:\\s*(\\w+)\\s*$` matches a colon, followed by optional whitespace,
     * then one or more word characters (captured in group 1), followed by optional whitespace,
     * and finally the end of the string. This ensures the abbreviation is detected
     * when typed at the end of the current text and followed by a space.
     */
    private static final Pattern ABBREVIATION_PATTERN = Pattern.compile(":\\s*(\\w+)\\s*$");

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
        initializeAbbreviations(); // Populate the initial set of abbreviations
        attachListeners();         // Attach this ChangeListener to all input TextAreas
    }

    // --- Private Helper Methods ---

    /**
     * Initializes the map with predefined abbreviations.
     * This method can be extended to load abbreviations from a file, database, etc.
     */
    private void initializeAbbreviations() {
        // Example abbreviations. Add more as needed.
        abbreviations.put("cc", "Chief Complaint");
        abbreviations.put("pi", "Present Illness");
        abbreviations.put("ros", "Review of Systems");
        abbreviations.put("pmh", "Past Medical History");
        abbreviations.put("s", "Subjective");
        abbreviations.put("o", "Objective");
        abbreviations.put("pe", "Physical Exam");
        abbreviations.put("a", "Assessment");
        abbreviations.put("p", "Plan");
        abbreviations.put("cmt", "Comment");
        // Add more abbreviations here for common medical terms or phrases
        abbreviations.put("dx", "Diagnosis");
        abbreviations.put("tx", "Treatment");
        abbreviations.put("rx", "Prescription");
        abbreviations.put("hpi", "History of Present Illness");
        abbreviations.put("fhx", "Family History");
        abbreviations.put("shx", "Social History");
        abbreviations.put("allergies", "Allergies");
        abbreviations.put("meds", "Medications");
        abbreviations.put("vs", "Vital Signs");
        abbreviations.put("cva", "Cerebrovascular Accident");
        abbreviations.put("mi", "Myocardial Infarction");
        abbreviations.put("dm", "Diabetes Mellitus");
        abbreviations.put("htn", "Hypertension");
        abbreviations.put("cad", "Coronary Artery Disease");
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

        // If the pattern is found at the end of the text
        if (matcher.find()) {
            String abbreviationKey = matcher.group(1).toLowerCase(); // Extract the 'word' part and convert to lowercase
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
        // It's important that this method does not trigger another change event
        // directly, which is handled by temporarily removing and re-adding the listener.
        applyAbbreviation(sourceTextArea, newValue);

        // Note: The main output area (`tempOutputArea` in `GDSEMR_frame`) is expected
        // to have its own listener that aggregates content from all `textAreas`.
        // When `textArea.setText(replacedText)` is called in `applyAbbreviation`,
        // it will trigger the `GDSEMR_frame`'s listener, thus updating the output area.
        // Therefore, no explicit update to `outputArea` is needed here to avoid redundancy
        // and potential circular updates.
    }

    // --- Optional Public Methods (for managing abbreviations dynamically) ---

    /**
     * Adds a new abbreviation or updates an existing one.
     * The key is converted to lowercase to ensure case-insensitive matching.
     * @param key The abbreviation trigger (e.g., "dx").
     * @param value The full text (e.g., "Diagnosis").
     */
    public void addAbbreviation(String key, String value) {
        this.abbreviations.put(key.toLowerCase(), value);
    }

    /**
     * Removes an abbreviation from the map.
     * The key is converted to lowercase to ensure consistency.
     * @param key The abbreviation trigger to remove.
     */
    public void removeAbbreviation(String key) {
        this.abbreviations.remove(key.toLowerCase());
    }

    /**
     * Retrieves the current map of abbreviations.
     * @return A unmodifiable map of abbreviations.
     */
    public Map<String, String> getAbbreviations() {
        return java.util.Collections.unmodifiableMap(abbreviations);
    }

	public void refreshAbbreviations() {
		// TODO Auto-generated method stub
		
	}
}
