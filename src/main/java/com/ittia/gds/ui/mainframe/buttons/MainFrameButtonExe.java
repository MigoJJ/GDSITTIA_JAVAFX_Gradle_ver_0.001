package com.ittia.gds.ui.mainframe.buttons;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.ittia.gds.GDSEMR_frame;

/**
 * Executes the actions for the main frame's north and south panel buttons.
 * This class centralizes all button action logic, using a data-driven approach 
 * with Maps to define and manage behaviors for single and double clicks.
 */
public class MainFrameButtonExe {

    // A simple functional interface for button actions.
    @FunctionalInterface
    private interface ButtonAction {
        void execute();
    }

    // Maps to store actions. The key is the button name.
    private static final Map<String, ButtonAction> northSingleClickActions = new HashMap<>();
    private static final Map<String, ButtonAction> northDoubleClickActions = new HashMap<>();
    private static final Map<String, ButtonAction> southSingleClickActions = new HashMap<>();
    private static final Map<String, ButtonAction> southDoubleClickActions = new HashMap<>();

    // Static initializer block to populate the maps with all button actions.
    static {
        populateNorthActions();
        populateSouthActions();
    }

    /**
     * Populates the maps with single and double-click actions for the North panel buttons.
     */
    private static void populateNorthActions() {
        // --- North Panel Single-Click Actions ---
        northSingleClickActions.put("Rescue", () -> showMessage("Rescue action to be defined later."));
        northSingleClickActions.put("Backup", () -> { /* No default single-click action */ });
        northSingleClickActions.put("Copy", () -> copyTextToClipboard(GDSEMR_frame.tempOutputArea.getText(), "Text copied!"));
        northSingleClickActions.put("CE", () -> {
            for (int i = 1; i <= 7; i++) {
                GDSEMR_frame.textAreas[i].setText(GDSEMR_frame.TEXT_AREA_TITLES[i] + "\t");
            }
        });
        northSingleClickActions.put("Clear", () -> {
        	for (int i = 0; i <= 9; i++) {
        		GDSEMR_frame.textAreas[i].setText(GDSEMR_frame.TEXT_AREA_TITLES[i] + "\t");
        	}
        });             
        northSingleClickActions.put("Exit", () -> System.exit(0));
        
        // --- North Panel Double-Click Actions ---
        northDoubleClickActions.put("Rescue", () -> showMessage("Double-click Rescue: Action to be defined later."));
        northDoubleClickActions.put("Backup", () -> showMessage("Double-click Backup: Saving to secondary backup location."));
        northDoubleClickActions.put("Copy", () -> copyTextToClipboard(GDSEMR_frame.tempOutputArea.getSelectedText(), "Selected text copied!"));
        northDoubleClickActions.put("CE", () -> {
            showMessage("Double-click CE: Resetting all text areas.");
            for (int i = 0; i < GDSEMR_frame.textAreas.length; i++) {
                GDSEMR_frame.textAreas[i].setText(GDSEMR_frame.TEXT_AREA_TITLES[i] + "\t");
            }
        });
        northDoubleClickActions.put("Clear", () -> showMessage("Double-click Clear: Clearing and resetting formatting."));
        
        northDoubleClickActions.put("Exit", () -> {
            if (JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        northDoubleClickActions.put("Abbreviation", () -> showMessage("Double-click Abbreviation: Opening abbreviation editor."));
        northDoubleClickActions.put("ICD-11", () -> showMessage("Double-click ICD-11: Opening detailed ICD-11 search."));
        northDoubleClickActions.put("KCD8", () -> showMessage("Double-click KCD8: Opening KCD8 advanced view."));
        northDoubleClickActions.put("Lab code", () -> showMessage("Double-click Lab code: Opening lab code history."));
        northDoubleClickActions.put("Lab sum", () -> showMessage("Double-click Lab sum: Opening lab summary report."));
        northDoubleClickActions.put("db", () -> showMessage("Double-click db: Opening database management console."));
        northDoubleClickActions.put("ittia_support", () -> showMessage("Double-click ittia_support: Opening support ticket form."));
    }

    /**
     * Populates the maps with single and double-click actions for the South panel buttons.
     */
    private static void populateSouthActions() {
        // Define indices for commonly used text areas for clarity.
        // Based on GDSEMR_frame.TEXT_AREA_TITLES
        final int pmhIndex = 3;        // "PMH>"
        final int assessmentIndex = 7; // "A>"
        final int planIndex = 8;       // "P>"

        // --- South Panel Single-Click Actions ---
        // These actions append a standard template to the appropriate text area.
        southSingleClickActions.put("F/U DM",       () -> appendToTextArea(planIndex, "F/U DM. Continue current medication. Check HbA1c in 3 months."));
        southSingleClickActions.put("F/U HTN",      () -> appendToTextArea(planIndex, "F/U HTN. Continue medication. Recheck BP next visit."));
        southSingleClickActions.put("F/U Chol",     () -> appendToTextArea(planIndex, "F/U Hyperlipidemia. Continue statin. Lipid panel in 6 months."));
        southSingleClickActions.put("F/U Thyroid",  () -> appendToTextArea(planIndex, "F/U Hypothyroidism. Continue levothyroxine. Check TSH in 6 months."));
        southSingleClickActions.put("Osteoporosis", () -> appendToTextArea(planIndex, "F/U Osteoporosis. Continue supplements. BMD in 2 years."));
        southSingleClickActions.put("URI",          () -> {
            appendToTextArea(assessmentIndex, "Upper Respiratory Infection (URI).");
            appendToTextArea(planIndex, "Symptomatic treatment. Encourage rest and hydration.");
        });
        southSingleClickActions.put("Allergy",      () -> appendToTextArea(pmhIndex, "Allergic rhinitis, seasonal."));
        southSingleClickActions.put("Injections",   () -> appendToTextArea(planIndex, "Administered [Vaccine/Medication Name] [Dose] [Route]."));
        southSingleClickActions.put("GDS RC",       () -> appendToTextArea(planIndex, "Scheduled for GDS routine checkup."));
        southSingleClickActions.put("공단검진",     () -> appendToTextArea(planIndex, "Scheduled for National Health Insurance checkup (공단검진)."));
        southSingleClickActions.put("F/U Edit",     () -> showMessage("Double-click to open the advanced follow-up editor."));

        // --- South Panel Double-Click Actions ---
        southDoubleClickActions.put("F/U DM",       () -> showMessage("Double-click F/U DM: Opening detailed diabetes follow-up."));
        southDoubleClickActions.put("F/U HTN",      () -> showMessage("Double-click F/U HTN: Opening hypertension history."));
        southDoubleClickActions.put("F/U Chol",     () -> showMessage("Double-click F/U Chol: Opening cholesterol trends."));
        southDoubleClickActions.put("F/U Thyroid",  () -> showMessage("Double-click F/U Thyroid: Opening thyroid lab analysis."));
        southDoubleClickActions.put("Osteoporosis", () -> showMessage("Double-click Osteoporosis: Opening bone density report."));
        southDoubleClickActions.put("URI",          () -> showMessage("Double-click URI: Opening URI treatment history."));
        southDoubleClickActions.put("Allergy",      () -> showMessage("Double-click Allergy: Opening allergy profile editor."));
        southDoubleClickActions.put("Injections",   () -> showMessage("Double-click Injections: Opening injection schedule."));
        southDoubleClickActions.put("GDS RC",       () -> showMessage("Double-click GDS RC: Opening GDS routine check summary."));
        southDoubleClickActions.put("공단검진",     () -> showMessage("Double-click 공단검진: Opening HC routine check report."));
        southDoubleClickActions.put("F/U Edit",     () -> showMessage("Double-click F/U Edit: Opening advanced follow-up editor."));
    }

    /**
     * Finds and executes a single-click action based on the button name and panel location.
     * @param btn The button name (e.g., "Copy", "Exit").
     * @param location The panel location ("north" or "south").
     */
    public static void executeSingleClick(String btn, String location) {
        Map<String, ButtonAction> actionMap = "north".equals(location) ? northSingleClickActions : southSingleClickActions;
        actionMap.getOrDefault(btn, () -> {}).execute(); // Safely executes the action or does nothing if not found.
    }

    /**
     * Finds and executes a double-click action based on the button name and panel location.
     * @param btn The button name (e.g., "Copy", "Exit").
     * @param location The panel location ("north" or "south").
     */
    public static void executeDoubleClick(String btn, String location) {
        Map<String, ButtonAction> actionMap = "north".equals(location) ? northDoubleClickActions : southDoubleClickActions;
        actionMap.getOrDefault(btn, () -> {}).execute(); // Safely executes the action or does nothing if not found.
    }

    // --- Helper Methods ---
    
    /**
     * A helper to append text to a specific JTextArea in a thread-safe way.
     * @param index The index of the text area in GDSEMR_frame.textAreas.
     * @param text  The text to append.
     */
    private static void appendToTextArea(int index, String text) {
        if (GDSEMR_frame.textAreas != null && index >= 0 && index < GDSEMR_frame.textAreas.length) {
            SwingUtilities.invokeLater(() -> {
                GDSEMR_frame.textAreas[index].append(text + "\n");
            });
        } else {
            System.err.println("Attempted to append to an invalid text area index: " + index);
        }
    }

    /**
     * Displays a simple information message dialog.
     * @param message The message to display.
     */
    private static void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    /**
     * Copies the given text to the system clipboard and shows a success message.
     * @param text The text to copy.
     * @param successMessage The confirmation message to show the user.
     */
    private static void copyTextToClipboard(String text, String successMessage) {
        if (text == null || text.isEmpty()) {
            showMessage("Nothing to copy.");
            return;
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                   .setContents(new StringSelection(text), null);
            showMessage(successMessage);
        } catch (Exception e) {
            showMessage("Copy failed: " + e.getMessage());
        }
    }
}
