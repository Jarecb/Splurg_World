package org.jarec.game.resources;

import org.jarec.data.creature.Splurg;
import org.jarec.gui.WorldFrame;
import org.jarec.gui.WorldPanel;
import org.jarec.util.PropertyHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Splurgs {

    // Singleton instance - eagerly initialized and final for thread safety
    private static final Splurgs INSTANCE = new Splurgs();

    // Thread-safe list to handle concurrent access if needed
    private final List<Splurg> splurgList = Collections.synchronizedList(new ArrayList<>());

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
        System.out.println(splurgList.size());
    }

    public List<Splurg> getSplurgs() {
        // Return a copy to preserve encapsulation
        synchronized (splurgList) {
            return new ArrayList<>(splurgList);
        }
    }

    public void clearSplurgs() {
        synchronized (splurgList) {
            splurgList.clear();
        }
    }

    public void moveSplurgs() {
        synchronized (splurgList) {
            for (Splurg splurg : splurgList) {
                splurg.move();
            }
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
}
