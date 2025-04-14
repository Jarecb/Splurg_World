package org.jarec.game.resources;

import org.jarec.data.Hive;
import org.jarec.data.Location;
import org.jarec.data.creature.Splurg;
import org.jarec.gui.WorldFrame;
import org.jarec.gui.WorldPanel;
import org.jarec.util.PropertyHandler;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static org.jarec.util.GameMath.calculateHypotenuse;

public class Splurgs {
    private static int deaths = 0;
    private static int spawns = 0;

    private final List<Splurg> splurgList = new ArrayList<>();

    private Splurgs() {
    }

    public void reorder() {
        Collections.shuffle(splurgList);
    }

    private static final Splurgs INSTANCE = new Splurgs();

    public static Splurgs getInstance() {
        return INSTANCE;
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
        Map<Hive, Long> livingCounts = splurgList.stream().collect(Collectors.groupingBy(Splurg::getHomeHive, Collectors.counting()));

        List<Hive> allHives = Hives.getInstance().getHives();

        for (Hive hive : allHives) {
            if ((!livingCounts.containsKey(hive) || livingCounts.get(hive) == 0) && hive.getEnergyReserve() < Integer.parseInt(PropertyHandler.get("hive.default.spawn.energy", "10"))) {
                hive.setColor(null);
            }
        }
    }

    public List<Splurg> getSplurgsInVicinity(Location targetPoint) {
        var searchRadius = Integer.parseInt(PropertyHandler.get("gui.mouse.click.detection.range", "20"));

        Comparator<Splurg> orderingComparator = Comparator.comparing((Splurg s) -> s.getHomeHive().getName(), String.CASE_INSENSITIVE_ORDER).thenComparing(Comparator.comparingInt(Splurg::getHealth).reversed());

        synchronized (splurgList) {
            return splurgList.stream().filter(splurg -> {
                return calculateHypotenuse(targetPoint, splurg.getLocation()) <= searchRadius;
            }).sorted(orderingComparator).toList();
        }
    }

    public void handleBreeding() {
        synchronized (splurgList) {
            List<Splurg> originalSplurgs = new ArrayList<>(splurgList);

            Map<Hive, List<Splurg>> splurgsByHive = originalSplurgs.stream().collect(Collectors.groupingBy(Splurg::getHomeHive));

            int spawnEnergyCost = Integer.parseInt(PropertyHandler.get("hive.default.spawn.energy", "10"));

            for (List<Splurg> sameHiveSplurgs : splurgsByHive.values()) {
                List<Splurg[]> breedingPairs = new ArrayList<>();
                Set<Splurg> used = new HashSet<>();

                for (int i = 0; i < sameHiveSplurgs.size(); i++) {
                    Splurg s1 = sameHiveSplurgs.get(i);
                    if (used.contains(s1)) continue;

                    for (int j = i + 1; j < sameHiveSplurgs.size(); j++) {
                        Splurg s2 = sameHiveSplurgs.get(j);
                        if (used.contains(s2)) continue;

                        boolean canBreed = !s1.isInCombat() && !s2.isInCombat() && s1.canBreed() && s2.canBreed() && s1.getEnergy() >= spawnEnergyCost && s2.getEnergy() >= spawnEnergyCost;

                        double distance = calculateHypotenuse(s1.getLocation(), s2.getLocation());
                        int maxDistance = s1.getSize().getValue() + s2.getSize().getValue();

                        if (canBreed && distance <= maxDistance) {
                            breedingPairs.add(new Splurg[]{s1, s2});
                            used.add(s1);
                            used.add(s2);
                            break; // Move on to next s1 since it's already used
                        }
                    }
                }

                for (Splurg[] pair : breedingPairs) {
                    Splurg p1 = pair[0];
                    Splurg p2 = pair[1];
                    Splurg child = new Splurg(p1, p2);

                    Location loc1 = p1.getLocation();
                    Location loc2 = p2.getLocation();
                    child.setLocation(new Location((loc1.getX() + loc2.getX()) / 2, (loc1.getY() + loc2.getY()) / 2));

                    String msg = String.format("A new Splurg called %s spawned from %s and %s of %s", child.getName(), p1.getName(), p2.getName(), p1.getHomeHive().getName());
                    WorldFrame.getInstance().updateStatus(msg);
                }
            }
        }
    }

    public Map<Hive, Long> getCounts() {
        synchronized (splurgList) {
            return splurgList.stream().collect(Collectors.groupingBy(Splurg::getHomeHive, Collectors.counting()));
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
            return splurgList.stream().collect(Collectors.groupingBy(Splurg::getHomeHive, Collectors.summingInt(splurg -> splurg.getEnergy() + splurg.getHomeHive().getEnergyReserve())));
        }
    }

    public int getAverageSplurgSize() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream().mapToInt(splurg -> splurg.getSize().getValue()).sum();

            return (int) (totalSize / splurgList.size());
        }
    }

    public int getAverageSplurgSpeed() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream().mapToInt(splurg -> splurg.getSpeed().getValue()).sum();

            return (int) (totalSize / splurgList.size());
        }
    }

    public int getAverageSplurgToughness() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream().mapToInt(splurg -> splurg.getToughness().getValue()).sum();

            return (int) (totalSize / splurgList.size());
        }
    }

    public int getAverageSplurgStrength() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream().mapToInt(splurg -> splurg.getStrength().getValue()).sum();

            return (int) (totalSize / splurgList.size());
        }
    }

    public int getAverageSplurgAggression() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream().mapToInt(splurg -> splurg.getAggression().getValue()).sum();

            return (int) (totalSize / splurgList.size());
        }
    }

    public int getAverageSplurgForaging() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream().mapToInt(splurg -> splurg.getForaging().getValue()).sum();

            return (int) (totalSize / splurgList.size());
        }
    }
}
