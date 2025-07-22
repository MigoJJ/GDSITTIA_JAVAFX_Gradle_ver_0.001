package com.ittia.gds.ui.mainframe.changestring;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.ittia.gds.ui.mainframe.file.File_copy;

public class EMRProcessor {
    private static final Map<String, String> REPLACEMENTS = new HashMap<>();
    private static final String[] TITLES = {"CC>", "PI>", "ROS>", "PMH>", "S>", "O>", "Physical Exam>", "A>", "P>", "Comment>"};
    private static final String FILE_PATH = com.ittia.gds.EntryDir.dbDir + "/chartplate/filecontrol/database/extracteddata.txt";
    private static final String BACKUP_PATH = com.ittia.gds.EntryDir.dbDir + File.separator + "tripikata" + File.separator + "rescue" + File.separator + "backup";
    private static final String TEMP_BACKUP_PATH = com.ittia.gds.EntryDir.dbDir + File.separator + "tripikata" + File.separator + "rescue" + File.separator + "backuptemp";

    static {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("replacements.put(")) {
                    String[] parts = line.split("\"");
                    if (parts.length >= 4) {
                        REPLACEMENTS.put(parts[1], parts[3]);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading abbreviations: " + e.getMessage());
        }
    }

    public static String processText(String text) {
        // Handle special abbreviations
        if (text.contains(":(")) {
            text = processAbbreviation(text);
        } else if (text.contains(":>")) {
            text = processPrescription(text);
        }

        // Replace current date placeholder
        text = text.replace(":cd ", Date_current.main("d"));

        // Perform bulk replacements
        for (Map.Entry<String, String> entry : REPLACEMENTS.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }

        // Organize titles
        text = organizeTitles(text);
        return "  " + text;
    }

    private static String processAbbreviation(String word) {
        String[] wordArray = word.split(" ");
        for (int i = 0; i < wordArray.length; i++) {
            if (wordArray[i].contains(":(")) {
                String replacement = wordArray[i];
                if (replacement.contains("d")) {
                    replacement = replacement.replace("d", "-day ago :cd )").replace(":(", " (onset ");
                } else if (replacement.contains("w")) {
                    replacement = replacement.replace("w", "-week ago :cd )").replace(":(", " (onset ");
                } else if (replacement.contains("m")) {
                    replacement = replacement.replace("m", "-month ago :cd )").replace(":(", " (onset ");
                } else if (replacement.contains("y")) {
                    replacement = replacement.replace("y", "-year ago :cd )").replace(":(", " (onset ");
                } else {
                    return word;
                }
                wordArray[i] = replacement;
                return String.join(" ", wordArray);
            }
        }
        return word;
    }

    private static String processPrescription(String word) {
        String[] wordArray = word.split(" ");
        StringBuilder retWord = new StringBuilder();
        for (String w : wordArray) {
            if (w.contains(":>")) {
                if (w.contains("1")) {
                    w = w.replace(":>1", " mg 1 tab p.o. q.d.");
                } else if (w.contains("2")) {
                    w = w.replace(":>2", " mg 1 tab p.o. b.i.d.");
                } else if (w.contains("3")) {
                    w = w.replace(":>3", " mg 1 tab p.o. t.i.d.");
                } else if (w.contains(":>0")) {
                    w = w.replace(":>0", " without medications");
                } else if (w.contains(":>4")) {
                    w = w.replace(":>4", " with medications");
                } else {
                    return retWord.toString().trim();
                }
            }
            retWord.append(w).append(" ");
        }
        return retWord.toString().trim();
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
}