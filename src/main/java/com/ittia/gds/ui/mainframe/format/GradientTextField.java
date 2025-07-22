package com.ittia.gds.ui.mainframe.format;

import javax.swing.JTextField;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Color;

public class GradientTextField extends JTextField {
    public GradientTextField(int columns) {
        super(columns);
        setOpaque(false);
        setForeground(Color.BLACK); // Ensure black font for readability
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        int width = getWidth();
        int height = getHeight();

        // Lighter Van Gogh-inspired gradient with pale gold and light sky blue
        GradientPaint gp = new GradientPaint(
            0, 0, new Color(255, 245, 157),    // Pale Gold (Sunflowers-inspired)
            0, height, new Color(135, 206, 250) // Light Sky Blue (Starry Night-inspired)
        );

        g2d.setPaint(gp);
        g2d.fillRect(0, 0, width, height);
        super.paintComponent(g2d);
        g2d.dispose();
    }
}