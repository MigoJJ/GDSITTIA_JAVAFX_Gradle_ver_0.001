package com.ittia.gds.ui.mainframe.buttons;

import com.ittia.gds.GDSittiaEntry;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Handles single and double-click actions for buttons in the GDSEMR_frame.
 * This class separates the action logic from the UI layout code.
 */
public class GDSFrameButtonExe {

    // The main application window.
    private final Stage primaryStage;

    // UI components from the main frame that the buttons will interact with.
    private final TextArea[] textAreas;
    private final TextArea tempOutputArea;

    private final PauseTransition clickTimer = new PauseTransition(Duration.millis(250));
    private int clickCount = 0;

    /**
     * Constructor for the button executor.
     *
     * @param primaryStage   The main application window (Stage).
     * @param textAreas      The array of input text areas from the main frame.
     * @param tempOutputArea The main output text area from the main frame.
     */
    public GDSFrameButtonExe(Stage primaryStage, TextArea[] textAreas, TextArea tempOutputArea) {
        this.primaryStage = primaryStage;
        this.textAreas = textAreas;
        this.tempOutputArea = tempOutputArea;
    }

    /**
     * Attaches click listeners to a given button to handle its actions.
     *
     * @param button        The button to attach the listener to.
     * @param actionCommand A string identifier for the button's action (e.g., "Save", "Clear").
     */
    public void attach(Button button, String actionCommand) {
        button.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                clickCount++;
                if (clickCount == 1) {
                    clickTimer.setOnFinished(e -> {
                        executeAction(actionCommand, false); // Single-click
                        clickCount = 0;
                    });
                    clickTimer.playFromStart();
                } else if (clickCount == 2) {
                    clickTimer.stop(); // Cancel single-click
                    executeAction(actionCommand, true); // Double-click
                    clickCount = 0;
                }
            }
        });
    }

    /**
     * Executes the appropriate action based on the command and click type.
     *
     * @param actionCommand The command associated with the button.
     * @param isDoubleClick True if the action was triggered by a double-click, false otherwise.
     */
    private void executeAction(String actionCommand, boolean isDoubleClick) {
        String clickType = isDoubleClick ? "Double-Click" : "Single-Click";
        System.out.println(actionCommand + " button: " + clickType + " detected.");

        switch (actionCommand) {
            case "SaveRescue": // Renamed from SaveRescue for consistency
                // TODO: Add save logic
                break;

            case "Load":
                LoadDiseaseCodeViewer.main(null);// TODO: Add load logic
                break;

            case "Clear":
                for (TextArea ta : textAreas) {
                    if (ta != null) ta.clear();
                }
                tempOutputArea.clear();
                System.out.println("All fields cleared.");
                break;

            case "Exit":
                if (isDoubleClick) {
                    // Double-click exits the entire program.
                    System.out.println("Exiting application.");
                    System.exit(0);
                } else {
                    // Single-click closes the current window and opens the main entry window.
                    System.out.println("Returning to main entry screen.");
                    primaryStage.close();

                    // Defer the creation of the new stage to ensure the current UI event is complete.
                    Platform.runLater(() -> {
                        try {
                            new GDSittiaEntry().start(new Stage());
                        } catch (Exception e) {
                            System.err.println("Failed to open main entry screen.");
                            e.printStackTrace();
                        }
                    });
                }
                break;
            
            case "CE":
            case "Submit":
                 // TODO: Add logic for these buttons
                break;

            default:
                System.out.println("Unknown action command: " + actionCommand);
        }
    }
}