package org.jarec.game;

import org.jarec.data.Hive;
import org.jarec.game.resources.Hives;
import org.jarec.game.resources.Splurgs;
import org.jarec.util.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class SimulationEngine {
    private static final Logger log = LoggerFactory.getLogger(SimulationEngine.class);
    private long lastSlowWarningTurn = -1;
    private boolean populationLimitReported;

    public void resetGuardrails() {
        lastSlowWarningTurn = -1;
        populationLimitReported = false;
    }

    public SimulationFrame step(int turn, int combatsThisTurn, int populationLimit) {
        SimulationState.setTurn(turn);
        long startedAt = System.nanoTime();
        advanceWorld();
        SimulationMetrics metrics = collectMetrics(turn, combatsThisTurn, populationLimit,
                System.nanoTime() - startedAt);
        reportGuardrails(metrics);
        return new SimulationFrame(metrics, createSnapshot());
    }

    private void advanceWorld() {
        Splurgs splurgs = Splurgs.getInstance();
        splurgs.reorder();
        splurgs.updateCharisma();
        splurgs.moveSplurgs();
        splurgs.removeDeadSplurgs();
        splurgs.healSplurgs();
        splurgs.depositEnergy();
        splurgs.handleBreeding();
        Hives.getInstance().spawnHives();
    }

    private SimulationMetrics collectMetrics(int turn, int combats, int populationLimit, long durationNanos) {
        Splurgs splurgs = Splurgs.getInstance();
        int totalEnergy = getTotalEnergy(splurgs);
        int population = Splurgs.getTotalSplurgs();
        int durationLimitMs = PropertyHandler.getInt("world.game.slow.tick.warning.ms", 250);
        return new SimulationMetrics(
                turn,
                population,
                Splurgs.getLiveHiveCount(),
                splurgs.getZombieCount(),
                totalEnergy,
                splurgs.getDeaths(),
                splurgs.getSpawns(),
                combats,
                durationNanos,
                population > populationLimit,
                durationNanos > durationLimitMs * 1_000_000L
        );
    }

    private int getTotalEnergy(Splurgs splurgs) {
        Map<Hive, Integer> creatureEnergy = splurgs.getTotalSplurgEnergyPerHive();
        List<Hive> hives = Hives.getInstance().getHives();
        return hives.stream()
                .mapToInt(hive -> hive.getEnergyReserve() + creatureEnergy.getOrDefault(hive, 0))
                .sum();
    }

    private WorldSnapshot createSnapshot() {
        Map<Hive, Integer> population = Splurgs.getSplurgsPerHive();
        List<WorldSnapshot.HiveView> hives = Hives.getInstance().getHives().stream()
                .map(hive -> new WorldSnapshot.HiveView(
                        hive.getLocation().getX(),
                        hive.getLocation().getY(),
                        hive.getColor(),
                        hive.getIcon(),
                        hive.getEnergyReserve(),
                        population.getOrDefault(hive, 0)))
                .toList();
        List<WorldSnapshot.SplurgView> splurgs = Splurgs.getInstance().getSplurgs().stream()
                .map(splurg -> new WorldSnapshot.SplurgView(
                        splurg.getLocation().getX(),
                        splurg.getLocation().getY(),
                        splurg.getSize().getValue(),
                        splurg.getHomeHive().getColor()))
                .toList();
        return new WorldSnapshot(hives, splurgs);
    }

    private void reportGuardrails(SimulationMetrics metrics) {
        if (metrics.populationLimitReached() && !populationLimitReported) {
            log.warn("Population limit exceeded at turn {}: {}", metrics.turn(), metrics.splurgCount());
            populationLimitReported = true;
        } else if (!metrics.populationLimitReached()) {
            populationLimitReported = false;
        }
        if (metrics.slowTick() && shouldReportSlowTick(metrics.turn())) {
            log.warn("Slow simulation tick {} took {} ms", metrics.turn(),
                    metrics.durationNanos() / 1_000_000L);
        }
    }

    private boolean shouldReportSlowTick(int turn) {
        if (lastSlowWarningTurn >= 0 && turn - lastSlowWarningTurn < 100) return false;
        lastSlowWarningTurn = turn;
        return true;
    }
}
