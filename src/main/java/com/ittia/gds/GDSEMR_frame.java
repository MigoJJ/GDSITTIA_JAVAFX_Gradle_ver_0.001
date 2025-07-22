package com.ittia.gds;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.ittia.gds.ui.mainframe.changestring.EMRProcessor;
import com.ittia.gds.ui.mainframe.format.MainFrameFormat;

/**
 * Main application window for the GDS EMR interface.
 * Ensures all text components use Consolas font explicitly via UIManager.
 */
public class GDSEMR_frame {
    private static final int FRAME_WIDTH = 1275;
    private static final int FRAME_HEIGHT = 1020;
    // CONSOLAS_FONT and CONSOLAS_BOLD_FONT are no longer strictly needed here
    // as UIManager sets default fonts, and MainFrameFormat handles bold.
    // Kept for potential future direct uses if desired, but not for global application.
    private static final Font CONSOLAS_FONT = new Font("DejaVu Sans Mono", Font.PLAIN, 11);
    private static final Color INPUT_TEXT_COLOR = new Color(0, 0, 0); // Black for input text
    public static JFrame frame;
    public static JTextArea[] textAreas;    public static JTextArea tempOutputArea;
    private JTextField gradientInputField;
    public static final String[] TEXT_AREA_TITLES = {
            "CC>", "PI>", "ROS>", "PMH>", "S>",
            "O>", "Physical Exam>", "A>", "P>", "Comment>"
    };

    /**
     * Sets the default font for various Swing components to Consolas using UIManager.
     * This method should be called once at the application startup.
     */
    public static void setGlobalConsolasFont() {
        Font conFont = new Font("DejaVu Sans Mono", Font.PLAIN, 11);
        UIManager.put("Button.font", new Font("DejaVu Sans Mono", Font.BOLD, 11)); // Buttons will use bold Consolas
        UIManager.put("Label.font", conFont);
        UIManager.put("TextField.font", conFont);
        UIManager.put("TextArea.font", conFont);
        UIManager.put("Panel.font", conFont); // Panels themselves don't display text, but can affect contained components
        UIManager.put("TitledBorder.font", conFont);
        UIManager.put("Table.font", conFont);
        UIManager.put("List.font", conFont);
        UIManager.put("Menu.font", conFont);
        UIManager.put("MenuItem.font", conFont);
        UIManager.put("ComboBox.font", conFont);
        UIManager.put("CheckBox.font", conFont);
        UIManager.put("RadioButton.font", conFont);
        UIManager.put("TabbedPane.font", conFont);
        UIManager.put("Viewport.font", conFont); // Used by JScrollPane's viewport
        // Add other components here as needed to ensure complete consistency
    }

    /**
     * Constructor for GDSEMR_frame. Initializes core UI components.
     */
    public GDSEMR_frame() {
        frame = new JFrame("GDS EMR Interface for Physician");
        textAreas = new JTextArea[TEXT_AREA_TITLES.length];
        tempOutputArea = new JTextArea();
        gradientInputField = MainFrameFormat.createGradientTextField(20);

        // No need to set font here, UIManager has already handled it.
        gradientInputField.setForeground(INPUT_TEXT_COLOR);
        tempOutputArea.setForeground(INPUT_TEXT_COLOR);
    }

    /**
     * Creates and displays the GUI.
     */
    public void createAndShowGUI() {
        configureFrame();
        initializePanels();
        registerListeners(); // Listeners remain important for functionality
        frame.setVisible(true);
    }

    /**
     * Configures the main JFrame properties.
     */
    private void configureFrame() {
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setLocation(348, 60);
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Initializes and adds all major panels to the frame.
     */
    private void initializePanels() {
        MainFrameFormat format = new MainFrameFormat();
        frame.add(format.createNorthPanel(), BorderLayout.NORTH);
        frame.add(format.createSouthPanel(), BorderLayout.SOUTH);
        frame.add(format.createCenterPanel(textAreas, TEXT_AREA_TITLES), BorderLayout.CENTER);
        frame.add(format.createWestPanel(tempOutputArea, gradientInputField), BorderLayout.WEST);
    }

    /**
     * Registers all necessary listeners. Font setting is now primarily handled by UIManager.
     */
    private void registerListeners() {
        for (int i = 0; i < textAreas.length; i++) {
            if (textAreas[i] != null) {
                // Font is already set via UIManager. No need for explicit setFont or double-check here.
                textAreas[i].setForeground(INPUT_TEXT_COLOR);
                textAreas[i].getDocument().addDocumentListener(new EMRProcessor.EMRDocumentListener(textAreas, tempOutputArea));
                textAreas[i].addMouseListener(new DoubleClickMouseListener());
                textAreas[i].addKeyListener(new FunctionKeyPress());
            }
        }
    }

    /**
     * Updates the text of a specific text area.
     * @param index The index of the text area to update.
     * @param text The text to append.
     */
    public static void setTextAreaText(int index, String text) {
        if (textAreas == null || index < 0 || index >= textAreas.length || textAreas[index] == null) {
            System.err.println("Invalid text area index or text areas not initialized for setTextAreaText.");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            textAreas[index].append(text);
        });
    }

    /**
     * Updates the temporary output area.
     * @param text The text to set in the output area.
     */
    public static void updateTempOutputArea(String text) {
        if (tempOutputArea != null) {
            SwingUtilities.invokeLater(() -> {
                tempOutputArea.setText(text);
            });
        }
    }

    /**
     * Handles function key press events.
     */
    private static class FunctionKeyPress extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12) {
                String functionKeyMessage = "F" + (keyCode - KeyEvent.VK_F1 + 1) + " key pressed - Action executed.";
                // You can add an action here, e.g., update tempOutputArea:
                // updateTempOutputArea(functionKeyMessage);
            }
        }
    }

    /**
     * Handles double-click events on text areas.
     */
    private static class DoubleClickMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                JTextArea source = (JTextArea) e.getSource();
                String text = source.getText();
                // You can add an action here, e.g., copy text to input field or process it
                // gradientInputField.setText(text);
            }
        }
    }

    /**
     * Launches the application.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        // IMPORTANT: Set global font BEFORE creating any Swing components
        setGlobalConsolasFont();

        SwingUtilities.invokeLater(() -> {
            GDSEMR_frame emrFrame = new GDSEMR_frame();
            try {
                emrFrame.createAndShowGUI();
            } catch (Exception e) {
                System.err.println("An error occurred during application startup:");
                e.printStackTrace();
            }
        });
    }
}