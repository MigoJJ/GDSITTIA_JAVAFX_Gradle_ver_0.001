package com.ittia.gds.ui.mainframe.buttons;

import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;

/**
 * Handles single and double-click actions for buttons in the GDSEMR_frame.
 * This class separates the action logic from the UI layout code.
 */
public class GDSFrameButtonExe {

    // UI components from the main frame that the buttons will interact with.
    private final TextArea[] textAreas;
    private final TextArea tempOutputArea;

    // A pause transition to help differentiate between single and double clicks.
    // A single click action is delayed slightly. If a second click occurs in that
    // time, the single-click action is cancelled, and a double-click is registered.
    private final PauseTransition clickTimer = new PauseTransition(Duration.millis(250));
    private int clickCount = 0;

    /**
     * Constructor for the button executor.
     *
     * @param textAreas      The array of input text areas from the main frame.
     * @param tempOutputArea The main output text area from the main frame.
     */
    public GDSFrameButtonExe(TextArea[] textAreas, TextArea tempOutputArea) {
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
            // We only care about the primary mouse button (usually the left button).
            if (event.getButton() == MouseButton.PRIMARY) {
                clickCount++;
                if (clickCount == 1) {
                    // On the first click, start the timer.
                    clickTimer.setOnFinished(e -> {
                        // If the timer finishes, it was a single click.
                        executeAction(actionCommand, false); // false for single-click
                        clickCount = 0; // Reset for the next click sequence.
                    });
                    clickTimer.playFromStart();
                } else if (clickCount == 2) {
                    // If a second click happens before the timer finishes, it's a double click.
                    clickTimer.stop(); // Cancel the pending single-click action.
                    executeAction(actionCommand, true); // true for double-click
                    clickCount = 0; // Reset for the next click sequence.
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
            case "SaveRescue":
                if (isDoubleClick) {
                    // TODO: Add double-click save logic (e.g., "Save As...")
                } else {
                    // TODO: Add single-click save logic (e.g., standard save)
                }
                break;

            case "Load":
                if (isDoubleClick) {
                    // TODO: Add double-click load logic
                } else {
                    // TODO: Add single-click load logic
                }
                break;

            case "Clear":
                // Typically, clear actions are the same for single or double-click.
                for (TextArea ta : textAreas) {
                    if (ta != null) {
                        ta.clear();
                    }
                }
                tempOutputArea.clear();
                System.out.println("All fields cleared.");
                break;

            case "Exit":
                if (isDoubleClick) {
                	System.exit(0);
                } else {
                	System.exit(0);                }
                break;

            case "...":
                if (isDoubleClick) {
                    // TODO: Add double-click submit logic (e.g., submit with high priority)
                } else {
                    // TODO: Add single-click submit logic
                }
                break;

            default:
                System.out.println("Unknown action command: " + actionCommand);
        }
    }
}
