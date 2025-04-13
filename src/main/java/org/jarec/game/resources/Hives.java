package org.jarec.game.resources;

import org.jarec.data.Hive;
import org.jarec.gui.WorldFrame;
import org.jarec.gui.WorldPanel;
import org.jarec.util.PropertyHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hives {

    // Singleton instance - eagerly initialized and final for thread safety
    private static final Hives INSTANCE = new Hives();

    // Thread-safe list to handle concurrent access if needed
    private final List<Hive> hiveList = Collections.synchronizedList(new ArrayList<>());

    // Private constructor to prevent instantiation
    private Hives() {}

    // Correctly declared as static to access without instance
    public static Hives getInstance() {
        return INSTANCE;
    }

    public void addHive(Hive hive) {
        if (hive != null) {
            hiveList.add(hive);
        }
    }

    public List<Hive> getHives() {
        // Return a copy to preserve encapsulation
        synchronized (hiveList) {
            return new ArrayList<>(hiveList);
        }
    }

    public void clearHives() {
        synchronized (hiveList) {
            hiveList.clear();
        }
    }

    public void spawnHives() {
        synchronized (hiveList) {
            for (Hive hive : hiveList) {
                hive.spawn();
            }
        }
    }

    public void drawHives() {
        WorldPanel worldPanel = WorldFrame.getInstance().getWorldPanel();

        int hiveSize = Integer.parseInt(PropertyHandler.get("hive.default.size", "20"));

        Graphics2D g2 = worldPanel.getBackgroundGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Stroke originalStroke = g2.getStroke();
        Stroke thickStroke = new BasicStroke(2);
        Font originalFont = g2.getFont();
        Font energyFont = new Font("Arial", Font.BOLD, 12);
        g2.setFont(energyFont);

        synchronized (hiveList) {
            for (Hive hive : hiveList) {
                int x = hive.getLocation().getX();
                int y = hive.getLocation().getY();

                int drawX = x - (hiveSize / 2);
                int drawY = y - (hiveSize / 2);

                // Fill circle if hive alive
                if (hive.getColor() != null) {
                    Color color = hive.getColor();
                    g2.setColor(color);
                    g2.fillOval(drawX, drawY, hiveSize, hiveSize);
                }

                // Draw border
                g2.setColor(new Color(0, 0, 0, 128));
                g2.setStroke(thickStroke);
                g2.drawOval(drawX, drawY, hiveSize, hiveSize);

                // Draw energy text (centered)
                String energyText = String.valueOf(hive.getEnergyReserve());
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

