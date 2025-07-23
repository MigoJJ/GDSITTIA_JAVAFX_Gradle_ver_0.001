package com.ittia.gds.ui.mainframe.changestring;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextArea;

/**
 * JavaFX ChangeListener to monitor text changes in TextAreas and update the output area.
 * Replaces the Swing-based DocumentListener for the GDS EMR interface.
 */
public class GDSEMR_DocumentListener implements ChangeListener<String> {
    private final TextArea[] textAreas;
    private final TextArea tempOutputArea;

    public GDSEMR_DocumentListener(TextArea[] textAreas, TextArea tempOutputArea) {
        this.textAreas = textAreas;
        this.tempOutputArea = tempOutputArea;
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        // Combine text from all text areas and update tempOutputArea
        StringBuilder combinedText = new StringBuilder();
        for (TextArea textArea : textAreas) {
            String text = textArea.getText();
            if (text != null && !text.trim().isEmpty()) {
                combinedText.append(text).append("\n");
            }
        }
        tempOutputArea.setText(combinedText.toString());
    }
}