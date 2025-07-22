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
public class MainFrameFormat2 {
    private static final int FRAME_WIDTH = 1275;
    private static final int FRAME_HEIGHT = 1020;
    // CONSOLAS_FONT is no longer needed here for default component fonts, as UIManager sets it.
    // However, CONSOLAS_BOLD_FONT is still used for specific button styling.
    private static final Font CONSOLAS_BOLD_FONT = new Font("DejaVu Sans Mono", Font.BOLD, 11);
    private static final Color HOVER_COLOR = new Color(255, 255, 255, 100);
    private static final Color PRESSED_COLOR = new Color(200, 220, 255, 150);
    private static final int BUTTON_CORNER_RADIUS = 15;
    // Color palette
    private static final Color SKY_BLUE = new Color(230, 240, 250); // #E6F0FA
    private static final Color SLATE_BLUE = new Color(70, 130, 180); // #4682B4
    private static final Color CYAN_LIGHT = new Color(176, 224, 230); // #B0E0E6
    private static final Color BLUE_MID = new Color(100, 149, 237); // #6495ED
    private static final Color GRAY_ACCENT = new Color(211, 211, 211); // #D3D3D3
    private static final Color OFF_WHITE = new Color(245, 246, 245); // #F5F6F5
    private static final Color INPUT_TEXT_COLOR = new Color(0, 0, 0); // Black for input text
    private static final Color BUTTON_TEXT_COLOR = new Color(74, 47, 0); // #4A2F00 Brown for button titles

    private JButton createFancyButton(String text, String panelType) {
        JButton button = new JButton(text) {
            private boolean hovered, pressed;

            {
                setFont(CONSOLAS_BOLD_FONT); // Still explicitly set Consolas bold for buttons
                setForeground(BUTTON_TEXT_COLOR);
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
                g2.setPaint(new GradientPaint(0, 0, CYAN_LIGHT, 0, getHeight(), BLUE_MID));
                g2.fill(shape);
                if (hovered) g2.setColor(HOVER_COLOR);
                if (pressed) g2.setColor(PRESSED_COLOR);
                if (hovered || pressed) g2.fill(shape);
                g2.setColor(GRAY_ACCENT);
                g2.draw(shape);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        return button;
    }

    private JPanel createGradientPanel(int height, int gridColumns, String[] buttonLabels, String panelType) {
        JPanel panel = new JPanel(new GridLayout(1, gridColumns)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, SKY_BLUE, 0, getHeight(), SLATE_BLUE));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
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
        Color[][] gradients = {
            {SKY_BLUE, SLATE_BLUE}, {BLUE_MID, GRAY_ACCENT},
            {SKY_BLUE, GRAY_ACCENT}, {BLUE_MID, SLATE_BLUE},
            {SKY_BLUE, OFF_WHITE}, {GRAY_ACCENT, SLATE_BLUE},
            {SKY_BLUE, OFF_WHITE}, {BLUE_MID, GRAY_ACCENT},
            {SKY_BLUE, OFF_WHITE}, {SLATE_BLUE, GRAY_ACCENT}
        };

        for (int i = 0; i < textAreas.length; i++) {
            textAreas[i] = new GradientTextArea(gradients[i][0], gradients[i][1]) {
                {
                    // Font is now set via UIManager. We only set foreground here.
                    setForeground(INPUT_TEXT_COLOR);
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
                g2d.setPaint(new GradientPaint(0, 0, SKY_BLUE, 0, getHeight(), SLATE_BLUE));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        westPanel.setPreferredSize(new Dimension(500, FRAME_HEIGHT));

        outputArea.setForeground(INPUT_TEXT_COLOR);
        outputArea.setOpaque(false);
        outputArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        outputArea.setEditable(true);

        inputField.setForeground(INPUT_TEXT_COLOR);
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
                // Font is now set via UIManager. We only set foreground here.
                setForeground(INPUT_TEXT_COLOR);
            }
        };
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(10, 10, 10, 10));
        return field;
    }
}