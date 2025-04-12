package org.jarec.game.resources;

import org.jarec.data.Location;
import org.jarec.data.Nest;
import org.jarec.data.creature.Splurg;
import org.jarec.gui.WorldFrame;
import org.jarec.gui.WorldPanel;
import org.jarec.util.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jarec.util.Math.calculateHypotenuse;

public class Splurgs {

    private static final Logger log = LoggerFactory.getLogger(Splurgs.class);

    private final List<Splurg> splurgList = new ArrayList<>();
    private Splurgs() {
    }

    private static class Holder {
        private static final Splurgs INSTANCE = new Splurgs();
    }

    public static Splurgs getInstance() {
        return Holder.INSTANCE;
    }

    public void addSplurg(Splurg splurg) {
        if (splurg != null) {
            synchronized (splurgList) {
                splurgList.add(splurg);
            }
        }
    }

    public List<Splurg> getSplurgs() {
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

    public void removeDeadSplurgs() {
        synchronized (splurgList) {
            splurgList.removeIf(splurg -> {
                boolean isDead = splurg.getHealth() <= 0;
                if (isDead) {
                    log.info("{} from {} has died", splurg.getName(), splurg.getHomeNest().getName());
                }
                return isDead;
            });
        }
    }

    public List<Splurg> getSplurgsInVicinity(Location targetPoint) {
        var searchRadius = Integer.parseInt(PropertyHandler.get("gui.mouse.click.detection.range", "20"));

        Comparator<Splurg> orderingComparator = Comparator
                .comparing((Splurg s) -> s.getHomeNest().getName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Comparator.comparingInt(Splurg::getHealth).reversed());

        synchronized (splurgList) {
            return splurgList.stream()
                    .filter(splurg -> {
                        return calculateHypotenuse(targetPoint, splurg.getLocation()) <= searchRadius;
                    })
                    .sorted(orderingComparator)
                    .collect(Collectors.toList());
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
