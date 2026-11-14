package org.jarec.gui;

import org.jarec.game.WorldSnapshot;
import org.jarec.util.PropertyHandler;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

public class WorldRenderer {
    public void render(WorldPanel panel, WorldSnapshot snapshot) {
        drawHives(panel, snapshot);
        drawSplurgs(panel, snapshot);
        panel.publish();
    }

    private void drawHives(WorldPanel panel, WorldSnapshot snapshot) {
        Graphics2D graphics = panel.getBackgroundGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Stroke originalStroke = graphics.getStroke();
        Font originalFont = graphics.getFont();
        graphics.setFont(new Font("Arial", Font.BOLD, 12));
        int size = PropertyHandler.getInt("hive.default.size", 60);
        for (WorldSnapshot.HiveView hive : snapshot.hives()) {
            drawHive(graphics, hive, size);
        }

        graphics.setFont(originalFont);
        graphics.setStroke(originalStroke);
        graphics.dispose();
    }

    private void drawHive(Graphics2D graphics, WorldSnapshot.HiveView hive, int size) {
        int x = hive.x();
        int y = hive.y();
        int drawX = x - size / 2;
        int drawY = y - size / 2;
        boolean hasSplurgs = hive.population() > 0;
        if (hive.icon() != null) {
            drawHiveIcon(graphics, hive, drawX, drawY, size, hasSplurgs);
        }
        drawHiveEnergy(graphics, hive, x, y, hasSplurgs);
    }

    private void drawHiveIcon(Graphics2D graphics, WorldSnapshot.HiveView hive, int x, int y,
                              int size, boolean occupied) {
        Composite original = graphics.getComposite();
        float alpha = occupied ? 0.5f : 0.1f;
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        graphics.drawImage(hive.icon(), x, y, size, size, null);
        graphics.setComposite(original);
    }

    private void drawHiveEnergy(Graphics2D graphics, WorldSnapshot.HiveView hive, int x, int y,
                                boolean occupied) {
        int energy = hive.energy();
        if (energy <= 0) return;
        FontMetrics metrics = graphics.getFontMetrics();
        int textX = x - metrics.stringWidth(String.valueOf(energy)) / 2;
        int textY = y + metrics.getAscent() / 2 - 2;
        float alpha = occupied ? 1f : 0.5f;
        graphics.setColor(new Color(0, 0, 0, alpha));
        graphics.drawString(String.valueOf(energy), textX, textY);
    }

    private void drawSplurgs(WorldPanel panel, WorldSnapshot snapshot) {
        Graphics2D graphics = panel.getBackgroundGraphics();
        int sizeMultiplier = PropertyHandler.getInt("splurg.default.size.multiplier", 2);
        for (WorldSnapshot.SplurgView splurg : snapshot.splurgs()) {
            drawSplurg(graphics, splurg, sizeMultiplier);
        }
        graphics.dispose();
    }

    private void drawSplurg(Graphics2D graphics, WorldSnapshot.SplurgView splurg, int sizeMultiplier) {
        int size = splurg.size() * sizeMultiplier;
        int x = splurg.x();
        int y = splurg.y();
        graphics.setColor(splurg.color());
        graphics.fillOval(x - size / 2, y - size / 2, size, size);
    }
}
