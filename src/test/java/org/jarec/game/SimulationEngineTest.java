package org.jarec.game;

import org.jarec.data.Hive;
import org.jarec.data.Location;
import org.jarec.data.WorldBounds;
import org.jarec.data.creature.Splurg;
import org.jarec.game.resources.Hives;
import org.jarec.game.resources.Splurgs;
import org.jarec.util.GameEvents;
import org.jarec.util.RandomInt;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationEngineTest {
    @AfterEach
    void clearSimulationState() {
        Splurgs.getInstance().clearSplurgs();
        Hives.getInstance().clearHives();
        GameEvents.setStatusListener(message -> {});
    }

    @Test
    void fixedSeedProducesRepeatableScenarioAndMetrics() {
        List<String> firstRun = runScenario(78L);
        List<String> repeatedRun = runScenario(78L);

        assertEquals(firstRun, repeatedRun);
        assertFalse(firstRun.equals(runScenario(79L)));
    }

    @Test
    void metricsExposePopulationLimitAndTickDuration() {
        WorldBounds.set(200, 200);
        Hives.getInstance().clearHives();
        Splurgs.getInstance().clearSplurgs();
        Hive redHive = createHive("Red Hive", 20, 100);
        Hive blueHive = createHive("Blue Hive", 180, 100);
        Hives.addHive(redHive);
        Hives.addHive(blueHive);
        RandomInt.setSeed(3L);
        new Splurg(redHive);
        new Splurg(blueHive);

        SimulationFrame frame = new SimulationEngine().step(1, 0, 1);
        SimulationMetrics metrics = frame.metrics();

        assertTrue(metrics.populationLimitReached());
        assertEquals(2, metrics.splurgCount());
        assertEquals(2, metrics.liveHiveCount());
        assertEquals(0, metrics.totalEnergy());
        assertEquals(0, metrics.deaths());
        assertEquals(2, metrics.spawns());
        assertEquals(0, metrics.combats());
        assertTrue(metrics.durationNanos() >= 0);
        assertEquals(2, frame.snapshot().hives().size());
        assertEquals(2, frame.snapshot().splurgs().size());
        assertThrows(UnsupportedOperationException.class, () -> frame.snapshot().hives().clear());
    }

    private List<String> runScenario(long seed) {
        WorldBounds.set(200, 200);
        Hives.getInstance().clearHives();
        Splurgs.getInstance().clearSplurgs();
        Hive redHive = createHive("Red Hive", 20, 100);
        Hive blueHive = createHive("Blue Hive", 180, 100);
        Hives.addHive(redHive);
        Hives.addHive(blueHive);
        RandomInt.setSeed(seed);
        new Splurg(redHive);
        new Splurg(blueHive);
        SimulationEngine engine = new SimulationEngine();
        List<String> results = new ArrayList<>();

        for (int turn = 1; turn <= 8; turn++) {
            SimulationMetrics metrics = engine.step(turn, 0, 500).metrics();
            assertFalse(metrics.populationLimitReached());
            results.add(metrics.turn() + ":" + metrics.splurgCount() + ":" + metrics.liveHiveCount()
                    + ":" + metrics.totalEnergy() + ":" + metrics.deaths() + ":" + metrics.spawns());
        }

        results.addAll(Splurgs.getInstance().getSplurgs().stream()
                .map(splurg -> splurg.getName() + ":" + splurg.getLocation().getX() + ":"
                        + splurg.getLocation().getY() + ":" + splurg.getHealth())
                .sorted()
                .toList());
        return List.copyOf(results);
    }

    private Hive createHive(String name, int x, int y) {
        return new Hive(new Location(x, y), Color.RED, name, false);
    }
}
