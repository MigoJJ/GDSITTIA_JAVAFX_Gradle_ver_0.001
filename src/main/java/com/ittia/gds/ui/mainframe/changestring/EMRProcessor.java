package com.ittia.gds.ui.mainframe.changestring;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.ittia.gds.ui.mainframe.changestring.abbreviation.DbManager;
import com.ittia.gds.ui.mainframe.file.File_copy;


public class EMRProcessor {
    // Make REPLACEMENTS non-final if you want to update it dynamically,
    // but for initial load, it can remain static.
    private static Map<String, String> REPLACEMENTS; // Now loaded from DB
    private static final String[] TITLES = {"CC>", "PI>", "ROS>", "PMH>", "S>", "O>", "Physical Exam>", "A>", "P>", "Comment>"};
    // Removed FILE_PATH since we are using DB
    private static final String BACKUP_PATH = com.ittia.gds.EntryDir.dbDir + File.separator + "tripikata" + File.separator + "rescue" + File.separator + "backup";
    private static final String TEMP_BACKUP_PATH = com.ittia.gds.EntryDir.dbDir + File.separator + "tripikata" + File.separator + "rescue" + File.separator + "backuptemp";

    // Add a DbManager instance
    private static DbManager dbManager;

    // Static initializer to load abbreviations from the database
    static {
        // Ensure EntryDir.dbDir is accessible here.
        // You might need to pass EntryDir.dbDir to a constructor
        // or a static initialization method if it's not available in static context.
        // For simplicity, assuming EntryDir.dbDir is available.
        dbManager = new DbManager(com.ittia.gds.EntryDir.dbDir);
        REPLACEMENTS = dbManager.getAllAbbreviations();
        
        // --- IMPORTANT: Initial Data Migration (Run ONLY ONCE) ---
        // If you still have your old extracteddata.txt and want to import
        // its contents into the new SQLite DB, uncomment and run the line below once.
        // After successful migration, you can comment it out again.
        // dbManager.importFromOldFile(com.ittia.gds.EntryDir.dbDir + "/chartplate/filecontrol/database/extracteddata.txt");
        // REPLACEMENTS = dbManager.getAllAbbreviations(); // Reload after import
        // ---------------------------------------------------------
    }

    public static String processText(String text) {
        // Handle special abbreviations by calling the transformer class
        if (text.contains(":(")) {
            text = EMRTextTransformer.processAbbreviation(text);
        } else if (text.contains(":>")) {
            text = EMRTextTransformer.processPrescription(text);
        }

        // Perform bulk replacements
        for (Map.Entry<String, String> entry : REPLACEMENTS.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }

        // Organize titles
        text = organizeTitles(text);
        return "  " + text;
    }

    private static String organizeTitles(String text) {
        for (String title : TITLES) {
            if (text.trim().equals(title)) {
                return "";
            }
        }
        return "\n" + text;
    }

    public static void updateTextArea(JTextArea[] textAreas, JTextArea tempOutputArea) throws IOException {
        StringBuilder outputData = new StringBuilder();
        Set<String> seenLines = new LinkedHashSet<>();

        for (JTextArea textArea : textAreas) {
            if (textArea != null) {
                String text = textArea.getText();
                if (text != null && !text.isEmpty()) {
                    for (String line : text.split("\n")) {
                        if (!seenLines.contains(line)) {
                            String processedLine = line.contains(":") ? processText(line) : organizeTitles(line);
                            seenLines.add(line);
                            outputData.append("\n").append(processedLine);
                        }
                    }
                }
            }
        }

        tempOutputArea.setText(outputData.toString());
        copyToClipboard(tempOutputArea);
        saveTextToFile(tempOutputArea);
    }

    private static void copyToClipboard(JTextArea textArea) {
        StringSelection stringSelection = new StringSelection(textArea.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    private static void saveTextToFile(JTextArea textArea) throws IOException {
        String textToSave = textArea.getText();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BACKUP_PATH))) {
            writer.write(textToSave);
        }
        File_copy.main(BACKUP_PATH, TEMP_BACKUP_PATH);
    }

    // Inner class EMRDocumentListener remains largely the same
    public static class EMRDocumentListener implements DocumentListener {
        private final JTextArea[] textAreas;
        private final JTextArea tempOutputArea;

        public EMRDocumentListener(JTextArea[] textAreas, JTextArea tempOutputArea) {
            this.textAreas = textAreas;
            this.tempOutputArea = tempOutputArea;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update();
        }

        private void update() {
            try {
                updateTextArea(textAreas, tempOutputArea);
            } catch (IOException e) {
                System.err.println("Error updating output area: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Public method to get the DbManager instance (useful for other parts of your app)
    public static DbManager getDbManager() {
        return dbManager;
    }

    // Method to force reload abbreviations from DB (if you modify them at runtime)
    public static void reloadAbbreviations() {
        REPLACEMENTS = dbManager.getAllAbbreviations();
        System.out.println("Abbreviations reloaded from database.");
    }
}