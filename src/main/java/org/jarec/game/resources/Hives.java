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
    private static final Hives INSTANCE = new Hives();

    // Thread-safe list to handle concurrent access if needed
    private final List<Hive> hiveList = Collections.synchronizedList(new ArrayList<>());

    private static final int HIVE_SIZE = Integer.parseInt(PropertyHandler.get("hive.default.size", "20"));

    private Hives() {
    }

    public static Hives getInstance() {
        return INSTANCE;
    }

    public void addHive(Hive hive) {
        if (hive != null) {
            hiveList.add(hive);
        }
    }

    public List<Hive> getHives() {
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

        Graphics2D g2 = worldPanel.getBackgroundGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Stroke originalStroke = g2.getStroke();
        Font originalFont = g2.getFont();
        Font energyFont = new Font("Arial", Font.BOLD, 12);
        g2.setFont(energyFont);

        synchronized (hiveList) {
            for (Hive hive : hiveList) {
                if (!hive.isZombie()) {
                    int x = hive.getLocation().getX();
                    int y = hive.getLocation().getY();

                    int drawX = x - (HIVE_SIZE / 2);
                    int drawY = y - (HIVE_SIZE / 2);

                    var hiveImage = hive.getIcon();

                    if (hiveImage != null) {
                        Composite originalComposite = g2.getComposite();
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                        g2.drawImage(hiveImage, drawX, drawY, HIVE_SIZE, HIVE_SIZE, null);
                        g2.setComposite(originalComposite);
                    }

                    // Draw energy text
                    String energyText = String.valueOf(hive.getEnergyReserve());
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(energyText);
                    int textHeight = fm.getAscent();
                    int textX = x - textWidth / 2;
                    int textY = y + textHeight / 2 - 2;

                    g2.setColor(Color.BLACK);
                    g2.drawString(energyText, textX, textY);
                }
            }
        }

        g2.setFont(originalFont);
        g2.setStroke(originalStroke);
        g2.dispose();
    }
}
