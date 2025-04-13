package org.jarec.game.resources;

import org.jarec.data.Nest;
import org.jarec.gui.WorldFrame;
import org.jarec.gui.WorldPanel;
import org.jarec.util.PropertyHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Nests {

    // Singleton instance - eagerly initialized and final for thread safety
    private static final Nests INSTANCE = new Nests();

    // Thread-safe list to handle concurrent access if needed
    private final List<Nest> nestList = Collections.synchronizedList(new ArrayList<>());

    // Private constructor to prevent instantiation
    private Nests() {}

    // Correctly declared as static to access without instance
    public static Nests getInstance() {
        return INSTANCE;
    }

    public void addNest(Nest nest) {
        if (nest != null) {
            nestList.add(nest);
        }
    }

    public List<Nest> getNests() {
        // Return a copy to preserve encapsulation
        synchronized (nestList) {
            return new ArrayList<>(nestList);
        }
    }

    public void clearNests() {
        synchronized (nestList) {
            nestList.clear();
        }
    }

    public void spawnNests() {
        synchronized (nestList) {
            for (Nest nest : nestList) {
                nest.spawn();
            }
        }
    }

    public void drawNests() {
        WorldPanel worldPanel = WorldFrame.getInstance().getWorldPanel();

        int nestSize = Integer.parseInt(PropertyHandler.get("nest.default.size", "20"));

        Graphics2D g2 = worldPanel.getBackgroundGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Stroke originalStroke = g2.getStroke();
        Stroke thickStroke = new BasicStroke(2);
        Font originalFont = g2.getFont();
        Font energyFont = new Font("Arial", Font.BOLD, 12);
        g2.setFont(energyFont);

        synchronized (nestList) {
            for (Nest nest : nestList) {
                int x = nest.getLocation().getX();
                int y = nest.getLocation().getY();

                int drawX = x - (nestSize / 2);
                int drawY = y - (nestSize / 2);

                // Fill circle if nest alive
                if (nest.getColor() != null) {
                    Color color = nest.getColor();
                    g2.setColor(color);
                    g2.fillOval(drawX, drawY, nestSize, nestSize);
                }

                // Draw border
                g2.setColor(new Color(0, 0, 0, 128));
                g2.setStroke(thickStroke);
                g2.drawOval(drawX, drawY, nestSize, nestSize);

                // Draw energy text (centered)
                String energyText = String.valueOf(nest.getFoodReserve());
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(energyText);
                int textHeight = fm.getAscent();

                int textX = x - textWidth / 2;
                int textY = y + textHeight / 2 - 2;

                g2.setColor(Color.BLACK); // Text color
                g2.drawString(energyText, textX, textY);
            }
        }

        g2.setFont(originalFont);
        g2.setStroke(originalStroke);
        g2.dispose();
    }

}

