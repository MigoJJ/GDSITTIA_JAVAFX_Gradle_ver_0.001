package com.ittia.gds.ui.mainframe.format;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JTextArea;

public class GradientTextArea extends JTextArea {
    private final Color startColor;
    private final Color endColor;

    // Constructor with gradient parameters
    public GradientTextArea(Color startColor, Color endColor) {
        super();
        this.startColor = startColor;
        this.endColor = endColor;
        setOpaque(false); // Important for gradient backgrounds
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();
        GradientPaint gp = new GradientPaint(
            0, 0, startColor,
            0, height, endColor
        );
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, width, height);
        super.paintComponent(g);
    }
}
