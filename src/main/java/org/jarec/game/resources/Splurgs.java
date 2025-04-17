package org.jarec.game.resources;

import org.jarec.data.Hive;
import org.jarec.data.Location;
import org.jarec.data.creature.Life;
import org.jarec.data.creature.Splurg;
import org.jarec.data.creature.Zombie;
import org.jarec.game.GameEndState;
import org.jarec.gui.WinnerPanel;
import org.jarec.gui.WorldFrame;
import org.jarec.gui.WorldPanel;
import org.jarec.util.PropertyHandler;

import javax.swing.*;
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

    public int getTotalSplurgs() {
        return getCounts().values().stream().mapToInt(Math::toIntExact).sum();
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
            return splurgList.stream()
                    .filter(splurg -> calculateHypotenuse(targetPoint, splurg.getLocation()) <= searchRadius)
                    .sorted(orderingComparator)
                    .toList();
        }
    }

    public void handleBreeding() {
        synchronized (splurgList) {
            List<Splurg> originalSplurgs = new ArrayList<>(splurgList);
            Map<Hive, List<Splurg>> splurgsByHive = groupByHive(originalSplurgs);
            int spawnEnergyCost = getSpawnEnergyCost();

            for (List<Splurg> sameHiveSplurgs : splurgsByHive.values()) {
                List<Splurg[]> breedingPairs = findBreedingPairs(sameHiveSplurgs, spawnEnergyCost);
                spawnChildrenFromPairs(breedingPairs);
            }
        }
    }

    private Map<Hive, List<Splurg>> groupByHive(List<Splurg> splurgs) {
        return splurgs.stream().collect(Collectors.groupingBy(Splurg::getHomeHive));
    }

    private int getSpawnEnergyCost() {
        return Integer.parseInt(PropertyHandler.get("hive.default.spawn.energy", "10"));
    }

    private List<Splurg[]> findBreedingPairs(List<Splurg> splurgs, int spawnEnergyCost) {
        List<Splurg[]> pairs = new ArrayList<>();
        Set<Splurg> used = new HashSet<>();

        for (int i = 0; i < splurgs.size(); i++) {
            Splurg s1 = splurgs.get(i);
            if (used.contains(s1)) continue;

            Optional<Splurg> partner = splurgs.subList(i + 1, splurgs.size()).stream()
                    .filter(s2 -> !used.contains(s2))
                    .filter(s2 -> canBreedTogether(s1, s2, spawnEnergyCost))
                    .findFirst();

            partner.ifPresent(s2 -> {
                pairs.add(new Splurg[]{s1, s2});
                used.add(s1);
                used.add(s2);
            });

        }

        return pairs;
    }

    private boolean canBreedTogether(Splurg s1, Splurg s2, int spawnEnergyCost) {
        if (!s1.canBreed() || !s2.canBreed()) return false;
        if (s1.getEnergy() < spawnEnergyCost || s2.getEnergy() < spawnEnergyCost) return false;

        double distance = calculateHypotenuse(s1.getLocation(), s2.getLocation());
        int maxDistance = s1.getSize().getValue() + s2.getSize().getValue();

        return distance <= maxDistance;
    }

    private void spawnChildrenFromPairs(List<Splurg[]> pairs) {
        for (Splurg[] pair : pairs) {
            Splurg p1 = pair[0];
            Splurg p2 = pair[1];
            Splurg child = new Splurg(p1, p2);

            Location mid = getMidpoint(p1.getLocation(), p2.getLocation());
            child.setLocation(mid);

            String msg = String.format(
                    "A new Splurg called %s spawned from %s and %s of %s",
                    child.getName(), p1.getName(), p2.getName(), p1.getHomeHive().getName()
            );

            WorldFrame.getInstance().updateStatus(msg);
        }
    }

    private Location getMidpoint(Location a, Location b) {
        return new Location((a.getX() + b.getX()) / 2, (a.getY() + b.getY()) / 2);
    }

    public Map<Hive, Integer> getCounts() {
        synchronized (splurgList) {
            return splurgList.stream()
                    .filter(splurg -> !(splurg instanceof Zombie)) // Filter out Zombies
                    .collect(Collectors.groupingBy(
                            Splurg::getHomeHive,
                            Collectors.collectingAndThen(
                                    Collectors.counting(),
                                    Long::intValue
                            )
                    ));
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
            return splurgList.stream().collect(Collectors.groupingBy(Splurg::getHomeHive, Collectors.summingInt(Life::getEnergy)));
        }
    }

    public int getAverageSplurgSize() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream()
                    .filter(splurg -> !(splurg instanceof Zombie))
                    .mapToInt(splurg -> splurg.getSize().getValue())
                    .sum();

            return (int) (totalSize / splurgList.size());
        }
    }

    public int getAverageSplurgSpeed() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream()
                    .filter(splurg -> !(splurg instanceof Zombie))
                    .mapToInt(splurg -> splurg.getSpeed().getValue())
                    .sum();

            return (int) (totalSize / splurgList.size());
        }
    }

    public int getAverageSplurgToughness() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream()
                    .filter(splurg -> !(splurg instanceof Zombie))
                    .mapToInt(splurg -> splurg.getToughness().getValue())
                    .sum();

            return (int) (totalSize / splurgList.size());
        }
    }

    public int getAverageSplurgStrength() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream()
                    .filter(splurg -> !(splurg instanceof Zombie))
                    .mapToInt(splurg -> splurg.getStrength().getValue())
                    .sum();

            return (int) (totalSize / splurgList.size());
        }
    }

    public int getAverageSplurgAggression() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream()
                    .filter(splurg -> !(splurg instanceof Zombie))
                    .mapToInt(splurg -> splurg.getAggression().getValue())
                    .sum();

            return (int) (totalSize / splurgList.size());
        }
    }

    public int getAverageSplurgForaging() {
        synchronized (splurgList) {
            if (splurgList.isEmpty()) {
                return 0;
            }

            double totalSize = splurgList.stream()
                    .filter(splurg -> !(splurg instanceof Zombie))
                    .mapToInt(splurg -> splurg.getForaging().getValue())
                    .sum();

            return (int) (totalSize / splurgList.size());
        }
    }

    public int getLiveHiveCount() {
        synchronized (splurgList) {
            return (int) splurgList.stream()
                    .collect(Collectors.groupingBy(Splurg::getHomeHive, Collectors.counting()))
                    .values().stream()
                    .filter(count -> count > 0)
                    .count();
        }
    }

    public Hive getWinningHive() {
        Map<Hive, Integer> counts = getCounts();

        List<Hive> liveHives = counts.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .toList();

        if (liveHives.size() == 1) {
            return liveHives.get(0);
        }

        if (getTotalSplurgs() > WorldFrame.getInstance().getMaxPopulation()) {
            SwingUtilities.invokeLater(() -> WinnerPanel.createAndShowWinnerPanel(GameEndState.STALEMATE));
        }
        return null; // No winner yet
    }
}
