package org.jarec.game.resources;

import org.jarec.data.Hive;
import org.jarec.data.Location;
import org.jarec.data.creature.Life;
import org.jarec.data.creature.Splurg;
import org.jarec.data.creature.Zombie;
import org.jarec.game.GameEndState;
import org.jarec.game.GameLoop;
import org.jarec.gui.WinnerPanel;
import org.jarec.gui.WorldFrame;
import org.jarec.gui.WorldPanel;
import org.jarec.util.PropertyHandler;
import org.jarec.util.RandomInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static org.jarec.util.GameMath.calculateHypotenuse;

public class Splurgs {
    private static final Logger log = LoggerFactory.getLogger(Splurgs.class);

    private int deaths = 0;
    private int spawns = 0;
    private int zombieDeaths = 0;
    private int zombieSpawns = 0;
    private static final int ZOMBIE_SPAWN_CHANCE = Integer.parseInt(PropertyHandler.get("zombie.default.change", "1000")) * Hives.getInstance().getHiveCount();

    private static final List<Splurg> splurgList = new CopyOnWriteArrayList<>();

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
        if (splurg != null && !splurgList.contains(splurg)) {
            splurgList.add(splurg);
            if (splurg instanceof Zombie) {
                zombieSpawns++;
            } else {
                spawns++;
            }
        }
    }

    public static int getTotalSplurgs() {
        return getSplurgsPerHive().entrySet().stream()
                .filter(entry -> !entry.getKey().isZombie())
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

    public static int getTotalInfectedSplurgs() {
        return (int) splurgList.stream()
                .filter(splurg -> !(splurg instanceof Zombie))
                .filter(Splurg::isInfected)
                .count();
    }

    public int getDeaths() {
        return deaths;
    }

    public int getSpawns() {
        return spawns;
    }

    public int getZombieSpawns() {
        return zombieSpawns;
    }

    public int getZombieDeaths() {
        return zombieDeaths;
    }

    public List<Splurg> getSplurgs() {
        return new ArrayList<>(splurgList);
    }

    public void clearSplurgs() {
        splurgList.clear();
        deaths = 0;
        spawns = 0;
        zombieDeaths = 0;
        zombieSpawns = 0;
    }

    public void moveSplurgs() {
        for (Splurg splurg : splurgList) {
            splurg.move();
        }
    }

    public void depositEnergy() {
        for (Splurg splurg : splurgList) {
            splurg.depositEnergy();
        }
    }

    public void healSplurgs() {
        for (Splurg splurg : splurgList) {
            splurg.heal();
        }
    }

    private static <T> Set<T> getDuplicates() {
        Set<T> seen = new HashSet<>();
        Set<T> duplicates = new HashSet<>();
        for (T item : (List<T>) Splurgs.splurgList) {
            if (!seen.add(item)) {
                duplicates.add(item);
            }
        }
        return duplicates;
    }

    public void removeDeadSplurgs() {
        logDuplicates();

        List<Splurg> deadSplurgs = new ArrayList<>();

        for (Splurg splurg : new ArrayList<>(splurgList)) {
            if (splurg.getHealth() > 0) continue;

            deadSplurgs.add(splurg);
            processDeadSplurg(splurg);

            if (shouldBecomeZombie(splurg)) {
                new Zombie(splurg);
            }

            splurgList.removeAll(deadSplurgs);
        }
    }

     // Useful for debugging
    private void logDuplicates() {
        var duplicates = getDuplicates();
        if (!duplicates.isEmpty()) {
            log.error("Duplicate Splurgs found {}", duplicates);
        }
    }

    private void processDeadSplurg(Splurg splurg) {
        boolean isZombie = splurg instanceof Zombie;
        if (isZombie) {
            zombieDeaths++;
        } else {
            deaths++;
        }

        String status = splurg.getName() + " of " + splurg.getHomeHive().getName() + " has died";
        WorldFrame.getInstance().updateStatus(status);
    }

    private boolean shouldBecomeZombie(Splurg splurg) {
        if (splurg instanceof Zombie) return false;
        if (!GameLoop.getInstance().areZombiesActive()) return false;

        return splurg.isInfected() ||
                (RandomInt.getRandomInt(ZOMBIE_SPAWN_CHANCE) % ZOMBIE_SPAWN_CHANCE == 0);
    }


    public List<Splurg> getSplurgsInVicinity(Location targetPoint) {
        var searchRadius = Integer.parseInt(PropertyHandler.get("gui.mouse.click.detection.range", "20"));

        Comparator<Splurg> orderingComparator = Comparator.comparing((Splurg s) -> s.getHomeHive().getName(), String.CASE_INSENSITIVE_ORDER).thenComparing(Comparator.comparingInt(Splurg::getHealth).reversed());

        return splurgList.stream()
                .filter(splurg -> calculateHypotenuse(targetPoint, splurg.getLocation()) <= searchRadius)
                .sorted(orderingComparator)
                .toList();
    }

    public void handleBreeding() {
        List<Splurg> originalSplurgs = new ArrayList<>(splurgList);
        Map<Hive, List<Splurg>> splurgsByHive = groupByHive(originalSplurgs);
        int spawnEnergyCost = getSpawnEnergyCost();

        for (List<Splurg> sameHiveSplurgs : splurgsByHive.values()) {
            List<Splurg[]> breedingPairs = findBreedingPairs(sameHiveSplurgs, spawnEnergyCost);
            spawnChildrenFromPairs(breedingPairs);
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

    public static Map<Hive, Integer> getSplurgsPerHive() {
        return splurgList.stream()
                .collect(Collectors.groupingBy(
                        Splurg::getHomeHive,
                        Collectors.collectingAndThen(
                                Collectors.counting(),
                                Long::intValue
                        )
                ));
    }

    public void drawSplurges() {
        WorldPanel worldPanel = WorldFrame.getInstance().getWorldPanel();
        Graphics2D g2 = worldPanel.getBackgroundGraphics();

        int splurgSizeMultiplier = Integer.parseInt(PropertyHandler.get("splurg.default.size.multiplier", "2"));

        for (Splurg splurg : splurgList) {
            int size = splurg.getSize().getValue() * splurgSizeMultiplier;
            int x = splurg.getLocation().getX();
            int y = splurg.getLocation().getY();
            Color color = splurg.getHomeHive().getColor();

            g2.setColor(color);
            g2.fillOval(x - (size / 2), y - (size / 2), size, size);
        }

        g2.dispose();
    }

    public Map<Hive, Integer> getTotalSplurgEnergyPerHive() {
        return splurgList.stream().collect(Collectors.groupingBy(Splurg::getHomeHive, Collectors.summingInt(Life::getEnergy)));
    }

    public int getAverageSplurgSize() {
        if (splurgList.isEmpty()) {
            return 0;
        }

        double totalSize = splurgList.stream()
                .filter(splurg -> !(splurg instanceof Zombie))
                .mapToInt(splurg -> splurg.getSize().getValue())
                .sum();

        return (int) (totalSize / splurgList.size());
    }

    public int getAverageSplurgSpeed() {
        if (splurgList.isEmpty()) {
            return 0;
        }

        double totalSize = splurgList.stream()
                .filter(splurg -> !(splurg instanceof Zombie))
                .mapToInt(splurg -> splurg.getSpeed().getValue())
                .sum();

        return (int) (totalSize / splurgList.size());
    }

    public int getAverageSplurgToughness() {
        if (splurgList.isEmpty()) {
            return 0;
        }

        double totalSize = splurgList.stream()
                .filter(splurg -> !(splurg instanceof Zombie))
                .mapToInt(splurg -> splurg.getToughness().getValue())
                .sum();

        return (int) (totalSize / splurgList.size());
    }

    public int getAverageSplurgStrength() {
        if (splurgList.isEmpty()) {
            return 0;
        }

        double totalSize = splurgList.stream()
                .filter(splurg -> !(splurg instanceof Zombie))
                .mapToInt(splurg -> splurg.getStrength().getValue())
                .sum();

        return (int) (totalSize / splurgList.size());
    }

    public int getAverageSplurgAggression() {
        if (splurgList.isEmpty()) {
            return 0;
        }

        double totalSize = splurgList.stream()
                .filter(splurg -> !(splurg instanceof Zombie))
                .mapToInt(splurg -> splurg.getAggression().getValue())
                .sum();

        return (int) (totalSize / splurgList.size());
    }

    public int getAverageSplurgForaging() {
        if (splurgList.isEmpty()) {
            return 0;
        }

        double totalSize = splurgList.stream()
                .filter(splurg -> !(splurg instanceof Zombie))
                .mapToInt(splurg -> splurg.getForaging().getValue())
                .sum();

        return (int) (totalSize / splurgList.size());
    }

    public int getAverageSplurgCharisma() {
        if (splurgList.isEmpty()) {
            return 0;
        }

        double totalSize = splurgList.stream()
                .filter(splurg -> !(splurg instanceof Zombie))
                .mapToInt(splurg -> splurg.getCharisma().getValue())
                .sum();

        return (int) (totalSize / splurgList.size());
    }

    public int getAverageSplurgLoner() {
        if (splurgList.isEmpty()) {
            return 0;
        }

        double totalSize = splurgList.stream()
                .filter(splurg -> !(splurg instanceof Zombie))
                .mapToInt(splurg -> splurg.getLoner().getValue())
                .sum();

        return (int) (totalSize / splurgList.size());
    }

    public static int getLiveHiveCount() {
        return (int) splurgList.stream()
                .collect(Collectors.groupingBy(Splurg::getHomeHive, Collectors.counting()))
                .values().stream()
                .filter(count -> count > 0)
                .count();
    }

    public Hive getWinningHive() {
        Map<Hive, Integer> counts = getSplurgsPerHive();

        List<Hive> liveHives = counts.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .toList();

        if (liveHives.size() == 1) {
            return liveHives.getFirst();
        }

        if (getTotalSplurgs() > WorldFrame.getMaxPopulation()) {
            SwingUtilities.invokeLater(() -> WinnerPanel.createAndShowWinnerPanel(GameEndState.STALEMATE));
        }
        return null; // No winner yet
    }

    public int getZombieCount() {
        return (int) splurgList.stream()
                .filter(Zombie.class::isInstance)
                .count();
    }

    public void updateCharisma() {
        splurgList.forEach(Splurg::calculateCharisma);
    }
}