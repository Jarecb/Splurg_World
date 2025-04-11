package org.jarec.game.resources;

import org.jarec.Main;
import org.jarec.data.Nest;
import org.jarec.data.creature.Splurg;
import org.jarec.gui.WorldFrame;
import org.jarec.gui.WorldPanel;
import org.jarec.util.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Splurgs {
    private static final Logger log = LoggerFactory.getLogger(Splurgs.class);

    // Singleton instance - eagerly initialized and final for thread safety
    private static final Splurgs INSTANCE = new Splurgs();

    // Thread-safe list to handle concurrent access if needed
    private final List<Splurg> splurgList = Collections.synchronizedList(new ArrayList<>());
    // List to track dead splurgs
    private final List<Splurg> deadSplurgs = new ArrayList<>();

    // Private constructor to prevent instantiation
    private Splurgs() {
    }

    // Correctly declared as static to access without instance
    public static Splurgs getInstance() {
        return INSTANCE;
    }

    public void addSplurg(Splurg splurg) {
        if (splurg != null) {
            splurgList.add(splurg);
        }
    }

    public List<Splurg> getSplurgs() {
        // Return a copy to preserve encapsulation
        synchronized (splurgList) {
            return new ArrayList<>(splurgList);
        }
    }

    public void findNearest(Splurg splurg) {
        // Implementation for finding the nearest splurg (if needed)
    }

    public void clearSplurgs() {
        synchronized (splurgList) {
            splurgList.clear();
        }
    }

    // Method to move splurgs and track dead splurgs
    public void moveSplurgs() {
        synchronized (splurgList) {
            // Temporary list to track splurgs to remove
            List<Splurg> toRemove = new ArrayList<>();

            // Move each splurg
            for (Splurg splurg : splurgList) {
                splurg.move();
            }
        }
    }

    public Map<Nest, Long> getCounts() {
        synchronized (splurgList) {
            return splurgList.stream()
                    .collect(Collectors.groupingBy(Splurg::getHomeNest, Collectors.counting()));
        }
    }

    public void drawSplurges() {
        WorldPanel worldPanel = WorldFrame.getInstance().getWorldPanel();

        var splurgSizeMultiplier = Integer.parseInt(PropertyHandler.get("splurg.default.size.multiplier", "2"));

        Graphics2D g2 = worldPanel.getBackgroundGraphics();
        synchronized (splurgList) {
            for (Splurg splurg : splurgList) {
                var splurgSize = splurg.getSize().getValue() * splurgSizeMultiplier;
                int x = splurg.getLocation().getX();
                int y = splurg.getLocation().getY();
                Color color = splurg.getHomeNest().getColor();

                g2.setColor(color);
                g2.fillOval(x - (splurgSize / 2), y - (splurgSize / 2), splurgSize, splurgSize);
            }
        }
        g2.dispose();
    }

    public void removeDeadSplurgs(){
        List<Splurg> toRemove = new ArrayList<>();

        synchronized (splurgList) {
            for (Splurg splurg : splurgList) {
                if (splurg.getHealth() <= 0) {
                    toRemove.add(splurg);
                    log.info("A Splurg from new {} has died", splurg.getHomeNest().getName());
                }
            }

            splurgList.removeAll(toRemove);
        }
    }
}
