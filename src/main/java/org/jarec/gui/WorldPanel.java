package org.jarec.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Coordinate 0:0 is top left corner
 */
public class WorldPanel extends JPanel {
    private static final Logger log = LoggerFactory.getLogger(WorldPanel.class);

    private BufferedImage canvas;
    private ArrayList<Drawable> drawables = new ArrayList<>();

    public WorldPanel() {
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                log.info("Mouse clicked at {}:{}", e.getX(), e.getY());
                Graphics2D g2 = canvas.createGraphics();
                g2.setColor(Color.RED);
                g2.fillOval(e.getX() - 3, e.getY() - 3, 6, 6);
                g2.dispose();
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (canvas == null) {
            canvas = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        }
        g.drawImage(canvas, 0, 0, null);

        // Draw any shapes added via code
        for (Drawable d : drawables) {
            d.draw((Graphics2D) canvas.getGraphics());
        }
    }

    public void addDrawable(Drawable d) {
        drawables.add(d);
        repaint();
    }
}
