package com.ittia.gds.ui.mainframe.format;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import com.ittia.gds.ui.mainframe.buttons.MainFrame_Button_north_south;

/**
 * Compact UI formatting for GDS EMR interface with a modern blue-gray palette and reduced button height.
 * Ensures all text components use Consolas font explicitly via UIManager.
 */
public class MainFrameFormat {
    private static final int FRAME_WIDTH = 1275;
    private static final int FRAME_HEIGHT = 1020;

    // --- NEW: High-Contrast Color Palette ---
    private static final Color PANEL_BACKGROUND_LIGHT = new Color(245, 245, 245); // Very light gray
    private static final Color PANEL_BACKGROUND_DARK = new Color(220, 220, 220);  // Light gray
    private static final Color ACCENT_BLUE_LIGHT = new Color(50, 150, 255);       // Bright blue
    private static final Color ACCENT_BLUE_DARK = new Color(20, 80, 200);        // Darker blue
    private static final Color BORDER_COLOR = new Color(180, 180, 180);          // Gray for borders
    private static final Color TEXT_INPUT = Color.BLACK;                         // Black for all input text, as requested
    private static final Color TEXT_ON_ACCENT = Color.WHITE;                     // White text for buttons
    private static final Color HOVER_COLOR = new Color(255, 255, 255, 70);       // Semi-transparent white for hover
    private static final Color PRESSED_COLOR = new Color(0, 0, 0, 70);           // Semi-transparent black for press

    // --- NEW: Font definitions ---
    // Font for input fields like JTextArea and JTextField
    private static final Font INPUT_TEXT_FONT = new Font("DejaVu Sans Mono", Font.PLAIN, 11);
    // Font for button labels
    private static final Font BUTTON_FONT = new Font("DejaVu Sans Mono", Font.BOLD, 11);
    private static final int BUTTON_CORNER_RADIUS = 15;


    private JButton createFancyButton(String text, String panelType) {
        JButton button = new JButton(text) {
            private boolean hovered, pressed;

            {
                setFont(BUTTON_FONT);
                setForeground(TEXT_ON_ACCENT); // MODIFIED: White text for better contrast on blue
                setBorder(new EmptyBorder(6, 15, 6, 15));
                setContentAreaFilled(false);
                setFocusPainted(false);
                setUI(new BasicButtonUI());
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { hovered = pressed = false; repaint(); }
                    @Override
                    public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                    @Override
                    public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        MainFrame_Button_north_south.EMR_B_1entryentry(text, panelType);
                        if (e.getClickCount() == 2) {
                            MainFrame_Button_north_south.EMR_B_2entryentry(text, panelType);
                        }
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                Shape shape = new RoundRectangle2D.Double(0, 0, getWidth()-1, getHeight()-1, BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);
                // MODIFIED: New gradient for buttons
                g2.setPaint(new GradientPaint(0, 0, ACCENT_BLUE_LIGHT, 0, getHeight(), ACCENT_BLUE_DARK));
                g2.fill(shape);
                if (hovered) g2.setColor(HOVER_COLOR);
                if (pressed) g2.setColor(PRESSED_COLOR);
                if (hovered || pressed) g2.fill(shape);
                g2.setColor(BORDER_COLOR); // MODIFIED: Use a clearer border color
                g2.draw(shape);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        return button;
    }

    private JPanel createGradientPanel(int height, int gridColumns, String[] buttonLabels, String panelType) {
        JPanel panel = new JPanel(new GridLayout(1, gridColumns, 5, 0)) { // Added horizontal gap
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                // MODIFIED: New subtle, light background gradient
                g2d.setPaint(new GradientPaint(0, 0, PANEL_BACKGROUND_LIGHT, 0, getHeight(), PANEL_BACKGROUND_DARK));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(new EmptyBorder(5, 5, 5, 5)); // Add some padding
        panel.setPreferredSize(new Dimension(FRAME_WIDTH, height));
        for (String label : buttonLabels) {
            panel.add(createFancyButton(label, panelType));
        }
        return panel;
    }

    public JPanel createNorthPanel() {
        String[] buttons = {"Rescue", "Backup", "Copy", "CE", "Clear", "Exit", "Abbreviation", "ICD-11", "KCD8", "Lab code", "Lab sum", "db", "ittia_support"};
        return createGradientPanel(45, 13, buttons, "north");
    }

    public JPanel createSouthPanel() {
        String[] buttons = {"F/U DM", "F/U HTN", "F/U Chol", "F/U Thyroid", "Osteoporosis", "URI", "Allergy", "Injections", "GDS RC", "공단검진", "F/U Edit"};
        return createGradientPanel(45, 11, buttons, "south");
    }

    public JPanel createCenterPanel(JTextArea[] textAreas, String[] titles) {
        JPanel centerPanel = new JPanel(new GridLayout(5, 2));
        centerPanel.setPreferredSize(new Dimension(900, 1000));
        
        // MODIFIED: Removed the complex gradient array for a single, high-contrast style.
        for (int i = 0; i < textAreas.length; i++) {
            // Using a simple gradient text area for all fields to ensure readability
            textAreas[i] = new GradientTextArea(PANEL_BACKGROUND_LIGHT, PANEL_BACKGROUND_DARK) {
                {
                    // NEW: Explicitly set font and color for high contrast
                    setFont(INPUT_TEXT_FONT);
                    setForeground(TEXT_INPUT);
                }
            };
            textAreas[i].setText(titles[i] + "\t");
            textAreas[i].setOpaque(false);
            textAreas[i].setBorder(new EmptyBorder(10, 10, 10, 10));
            
            JScrollPane scrollPane = new JScrollPane(textAreas[i]);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            centerPanel.add(scrollPane);
        }
        return centerPanel;
    }

    public JPanel createWestPanel(JTextArea outputArea, JTextField inputField) {
        JPanel westPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                // MODIFIED: Use the new light background gradient
                g2d.setPaint(new GradientPaint(0, 0, PANEL_BACKGROUND_LIGHT, 0, getHeight(), PANEL_BACKGROUND_DARK));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        westPanel.setPreferredSize(new Dimension(500, FRAME_HEIGHT));

        // MODIFIED: Apply new font and color settings
        outputArea.setFont(INPUT_TEXT_FONT);
        outputArea.setForeground(TEXT_INPUT);
        outputArea.setOpaque(false);
        outputArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        outputArea.setEditable(true);

        // MODIFIED: Apply new font and color settings
        inputField.setFont(INPUT_TEXT_FONT);
        inputField.setForeground(TEXT_INPUT);
        inputField.setOpaque(false);
        inputField.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        outputScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        outputScrollPane.setOpaque(false);
        outputScrollPane.getViewport().setOpaque(false);
        outputScrollPane.setBorder(null);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setOpaque(false);
        inputPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        inputPanel.add(inputField, BorderLayout.CENTER);

        westPanel.add(outputScrollPane, BorderLayout.CENTER);
        westPanel.add(inputPanel, BorderLayout.SOUTH);
        return westPanel;
    }

    public static JTextField createGradientTextField(int columns) {
        JTextField field = new GradientTextField(columns) {
            {
                // NEW: Explicitly set font and color
                setFont(INPUT_TEXT_FONT);
                setForeground(TEXT_INPUT);
            }
        };
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(10, 10, 10, 10));
        return field;
    }
    
    // NOTE: You will need to make sure the GradientTextArea and GradientTextField
    // classes exist and can accept these color arguments in their constructors.
    // For example:
    // class GradientTextArea extends JTextArea {
    //     private Color color1, color2;
    //     public GradientTextArea(Color c1, Color c2) {
    //         super();
    //         this.color1 = c1;
    //         this.color2 = c2;
    //     }
    //     @Override
    //     protected void paintComponent(Graphics g) {
    //         Graphics2D g2d = (Graphics2D) g;
    //         g2d.setPaint(new GradientPaint(0, 0, color1, 0, getHeight(), color2));
    //         g2d.fillRect(0, 0, getWidth(), getHeight());
    //         super.paintComponent(g);
    //     }
    // }
    // class GradientTextField extends JTextField { /* ... similar implementation ... */ }
}