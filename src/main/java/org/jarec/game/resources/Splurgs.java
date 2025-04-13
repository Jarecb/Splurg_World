package org.jarec.game.resources;

import org.jarec.data.Hive;
import org.jarec.data.Location;
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

import static org.jarec.util.GameMath.calculateHypotenuse;

public class Splurgs {

    private static final Logger log = LoggerFactory.getLogger(Splurgs.class);

    private static int deaths = 0;
    private static int spawns = 0;

    private final List<Splurg> splurgList = new ArrayList<>();
    private Splurgs() {
    }

    public void reorder() {
        Collections.shuffle(splurgList);
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
                spawns++;
            }
        }
    }

    public int getDeaths() {
        return deaths;
    }

    public int getSpawns() {
        return spawns;
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

    public void depositEnergy() {
        synchronized (splurgList) {
            for (Splurg splurg : splurgList) {
                splurg.depositEnergy();
            }
        }
    }

    public void healSplurgs() {
        synchronized (splurgList) {
            for (Splurg splurg : splurgList) {
                splurg.heal();
            }
        }
    }

    public void removeDeadSplurgs() {
        synchronized (splurgList) {
            splurgList.removeIf(splurg -> {
                boolean isDead = splurg.getHealth() <= 0;
                if (isDead) {
                    var statusMessage = splurg.getName() + " of " + splurg.getHomeHive().getName() + " has died";
                    WorldFrame.getInstance().updateStatus(statusMessage);
                    deaths++;
                }
                return isDead;
            });
            updateHiveColorsForEmptyHives();
        }
    }

    private void updateHiveColorsForEmptyHives() {
        Map<Hive, Long> livingCounts = splurgList.stream()
                .collect(Collectors.groupingBy(Splurg::getHomeHive, Collectors.counting()));

        List<Hive> allHives = Hives.getInstance().getHives();

        for (Hive hive : allHives) {
            if ((!livingCounts.containsKey(hive) || livingCounts.get(hive) == 0) &&
                    hive.getEnergyReserve() < Integer.parseInt(PropertyHandler.get("hive.default.spawn.energy", "10"))) {
                hive.setColor(null);
            }
        }
    }

    public List<Splurg> getSplurgsInVicinity(Location targetPoint) {
        var searchRadius = Integer.parseInt(PropertyHandler.get("gui.mouse.click.detection.range", "20"));

        Comparator<Splurg> orderingComparator = Comparator
                .comparing((Splurg s) -> s.getHomeHive().getName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Comparator.comparingInt(Splurg::getHealth).reversed());

        synchronized (splurgList) {
            return splurgList.stream()
                    .filter(splurg -> {
                        return calculateHypotenuse(targetPoint, splurg.getLocation()) <= searchRadius;
                    })
                    .sorted(orderingComparator)
                    .toList();
        }
    }

    public void handleBreeding() {
        synchronized (splurgList) {
            // Make a snapshot of the current splurgs to prevent recursive breeding
            List<Splurg> originalSplurgs = new ArrayList<>(splurgList);

            Map<Hive, List<Splurg>> splurgsByHive = originalSplurgs.stream()
                    .collect(Collectors.groupingBy(Splurg::getHomeHive));

            for (Map.Entry<Hive, List<Splurg>> hiveEntry : splurgsByHive.entrySet()) {
                List<Splurg> sameHiveSplurgs = new ArrayList<>(hiveEntry.getValue());
                List<Splurg[]> breedingPairs = new ArrayList<>();
                List<Splurg> alreadyUsed = new ArrayList<>();

                for (int i = 0; i < sameHiveSplurgs.size(); i++) {
                    for (int j = i + 1; j < sameHiveSplurgs.size(); j++) {
                        Splurg splurg1 = sameHiveSplurgs.get(i);
                        Splurg splurg2 = sameHiveSplurgs.get(j);

                        if (alreadyUsed.contains(splurg1) || alreadyUsed.contains(splurg2)) continue;

                        int sizeDistanceThreshold = splurg1.getSize().getValue() + splurg2.getSize().getValue();
                        double distance = calculateHypotenuse(splurg1.getLocation(), splurg2.getLocation());

                        int spawnEnergyCost = Integer.parseInt(PropertyHandler.get("hive.default.spawn.energy", "10"));

                        if (distance <= sizeDistanceThreshold &&
                                splurg1.getEnergy() >= spawnEnergyCost &&
                                splurg2.getEnergy() >= spawnEnergyCost &&
                                splurg1.canBreed() && splurg2.canBreed() &&
                                !splurg1.isInCombat() && !splurg2.isInCombat()) {

                            breedingPairs.add(new Splurg[]{splurg1, splurg2});
                            alreadyUsed.add(splurg1);
                            alreadyUsed.add(splurg2);
                        }
                    }
                }

                for (Splurg[] pair : breedingPairs) {
                    Splurg parent1 = pair[0];
                    Splurg parent2 = pair[1];

                    Splurg child = new Splurg(parent1, parent2);
                    int midX = (parent1.getLocation().getX() + parent2.getLocation().getX()) / 2;
                    int midY = (parent1.getLocation().getY() + parent2.getLocation().getY()) / 2;
                    child.setLocation(new Location(midX, midY));

                    var statusMessage = "A new Splurg called " + child.getName() + " spawned from " + parent1.getName() + " and " + parent2.getName() + " of " + parent1.getHomeHive().getName();
                    WorldFrame.getInstance().updateStatus(statusMessage);

                }
            }
        }
    }

    public Map<Hive, Long> getCounts() {
        synchronized (splurgList) {
            return splurgList.stream()
                    .collect(Collectors.groupingBy(Splurg::getHomeHive, Collectors.counting()));
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
                Color color = splurg.getHomeHive().getColor();

                g2.setColor(color);
                g2.fillOval(x - (size / 2), y - (size / 2), size, size);
            }
        }

        g2.dispose();
    }

    public Map<Hive, Integer> getTotalEnergyPerHive() {
        synchronized (splurgList) {
            return splurgList.stream()
                    .collect(Collectors.groupingBy(
                            Splurg::getHomeHive,
                            Collectors.summingInt(splurg -> splurg.getEnergy() + splurg.getHomeHive().getEnergyReserve())
                    ));
        }
    }

    public int getAverageSplurgSize() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream()
                    .mapToInt(splurg -> splurg.getSize().getValue())
                    .sum();

            return (int)(totalSize / splurgList.size());
        }
    }

    public int getAverageSplurgSpeed() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream()
                    .mapToInt(splurg -> splurg.getSpeed().getValue())
                    .sum();

            return (int)(totalSize / splurgList.size());
        }
    }

    public int getAverageSplurgToughness() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream()
                    .mapToInt(splurg -> splurg.getToughness().getValue())
                    .sum();

            return (int)(totalSize / splurgList.size());
        }
    }

    public int getAverageSplurgStrength() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream()
                    .mapToInt(splurg -> splurg.getStrength().getValue())
                    .sum();

            return (int)(totalSize / splurgList.size());
        }
    }

    public int getAverageSplurgAggression() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream()
                    .mapToInt(splurg -> splurg.getAggression().getValue())
                    .sum();

            return (int)(totalSize / splurgList.size());
        }
    }

    public int getAverageSplurgForaging() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream()
                    .mapToInt(splurg -> splurg.getForaging().getValue())
                    .sum();

            return (int)(totalSize / splurgList.size());
        }
    }
}
