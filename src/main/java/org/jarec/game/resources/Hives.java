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
    private static Hive zombieHive;

    // Thread-safe list to handle concurrent access if needed
    private static final List<Hive> hiveList = Collections.synchronizedList(new ArrayList<>());

    private static final int HIVE_SIZE = Integer.parseInt(PropertyHandler.get("hive.default.size", "20"));
    private static int activeHiveCount;

    private Hives() {
    }

    public static Hives getInstance() {
        return INSTANCE;
    }

    public static Hive getZombieHive() {
        return zombieHive;
    }

    public static void addHive(Hive hive) {
        if (hive != null) {
            if (hive.isZombie()) {
                zombieHive = hive;
                return;
            }
            hiveList.add(hive);
            activeHiveCount++;
        }
    }

    public List<Hive> getHives() {
        synchronized (hiveList) {
            return hiveList.stream()
                    .filter(hive -> !hive.isZombie()) // Filter out zombie hives
                    .toList();
        }
    }


    public void clearHives() {
        synchronized (hiveList) {
            hiveList.clear();
            zombieHive = null;
            activeHiveCount = 0;
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

        var splurgsPerHive = Splurgs.getSplurgsPerHive();

        synchronized (hiveList) {
            for (Hive hive : hiveList) {
                if (!hive.isZombie()) {
                    int x = hive.getLocation().getX();
                    int y = hive.getLocation().getY();

                    int drawX = x - (HIVE_SIZE / 2);
                    int drawY = y - (HIVE_SIZE / 2);

                    var hiveImage = hive.getIcon();

                    var splurgsInHive = splurgsPerHive.get(hive);

                    if (hiveImage != null) {
                        float alpha = 0.5f;
                        if (!splurgsPerHive.containsKey(hive) || splurgsInHive <= 0) {
                            alpha = 0.1f;
                        }
                        Composite originalComposite = g2.getComposite();
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                        g2.drawImage(hiveImage, drawX, drawY, HIVE_SIZE, HIVE_SIZE, null);
                        g2.setComposite(originalComposite);
                    }

                    // Draw energy text
                    var hiveEnergy = hive.getEnergyReserve();
                    if (hiveEnergy > 0) {
                        float alpha = 1f;
                        if (!splurgsPerHive.containsKey(hive) || splurgsInHive <= 0) {
                            alpha = 0.5f;
                        }
                        FontMetrics fm = g2.getFontMetrics();
                        int textWidth = fm.stringWidth(String.valueOf(hiveEnergy));
                        int textHeight = fm.getAscent();
                        int textX = x - textWidth / 2;
                        int textY = y + textHeight / 2 - 2;

                        g2.setColor(new Color(0, 0, 0, alpha));
                        g2.drawString(String.valueOf(hiveEnergy), textX, textY);
                    }
                }
            }
        }

        g2.setFont(originalFont);
        g2.setStroke(originalStroke);
        g2.dispose();
    }

    public int getHiveCount() {
        return activeHiveCount;
    }
}
