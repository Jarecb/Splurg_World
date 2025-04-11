package org.jarec.game.resources;

import org.jarec.data.Nest;
import org.jarec.data.creature.Splurg;
import org.jarec.gui.WorldFrame;
import org.jarec.gui.WorldPanel;
import org.jarec.util.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Splurgs {

    private static final Logger log = LoggerFactory.getLogger(Splurgs.class);

    // Thread-safe list for managing splurgs
    private final List<Splurg> splurgList = new ArrayList<>();

    // Private constructor to prevent external instantiation
    private Splurgs() {
    }

    /**
     * Holder class for lazy-loaded singleton instance.
     */
    private static class Holder {
        private static final Splurgs INSTANCE = new Splurgs();
    }

    /**
     * Gets the singleton instance of Splurgs.
     */
    public static Splurgs getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Adds a Splurg to the list.
     */
    public void addSplurg(Splurg splurg) {
        if (splurg != null) {
            synchronized (splurgList) {
                splurgList.add(splurg);
            }
        }
    }

    /**
     * Returns a defensive copy of all splurgs.
     */
    public List<Splurg> getSplurgs() {
        synchronized (splurgList) {
            return new ArrayList<>(splurgList);
        }
    }

    /**
     * Clears all splurgs from the list.
     */
    public void clearSplurgs() {
        synchronized (splurgList) {
            splurgList.clear();
        }
    }

    /**
     * Moves all splurgs.
     */
    public void moveSplurgs() {
        synchronized (splurgList) {
            for (Splurg splurg : splurgList) {
                splurg.move();
            }
        }
    }

    /**
     * Removes all dead splurgs and logs their death.
     */
    public void removeDeadSplurgs() {
        synchronized (splurgList) {
            splurgList.removeIf(splurg -> {
                boolean isDead = splurg.getHealth() <= 0;
                if (isDead) {
                    log.info("A Splurg from {} has died", splurg.getHomeNest().getName());
                }
                return isDead;
            });
        }
    }

    /**
     * Returns a count of Splurgs per Nest.
     */
    public Map<Nest, Long> getCounts() {
        synchronized (splurgList) {
            return splurgList.stream()
                    .collect(Collectors.groupingBy(Splurg::getHomeNest, Collectors.counting()));
        }
    }

    /**
     * Renders all Splurgs.
     */
    public void drawSplurges() {
        WorldPanel worldPanel = WorldFrame.getInstance().getWorldPanel();
        Graphics2D g2 = worldPanel.getBackgroundGraphics();

        int splurgSizeMultiplier = Integer.parseInt(PropertyHandler.get("splurg.default.size.multiplier", "2"));

        synchronized (splurgList) {
            for (Splurg splurg : splurgList) {
                int size = splurg.getSize().getValue() * splurgSizeMultiplier;
                int x = splurg.getLocation().getX();
                int y = splurg.getLocation().getY();
                Color color = splurg.getHomeNest().getColor();

                g2.setColor(color);
                g2.fillOval(x - (size / 2), y - (size / 2), size, size);
            }
        }

        g2.dispose();
    }
}
