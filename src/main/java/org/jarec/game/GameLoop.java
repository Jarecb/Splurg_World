package org.jarec.game;

import org.jarec.data.Hive;
import org.jarec.data.WorldBounds;
import org.jarec.game.resources.Hives;
import org.jarec.game.resources.Splurgs;
import org.jarec.gui.WorldFrame;
import org.jarec.gui.WorldPanel;
import org.jarec.gui.WorldRenderer;
import org.jarec.util.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GameLoop {
    private static final Logger log = LoggerFactory.getLogger(GameLoop.class);

    private static final GameLoop INSTANCE = new GameLoop();
    private final SimulationEngine simulationEngine = new SimulationEngine();
    private final WorldRenderer worldRenderer = new WorldRenderer();

    private volatile int turn = 0;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile boolean started = false;
    private volatile long generation = 0;
    private volatile Thread loopThread;
    private final AtomicInteger loopSleepTime = new AtomicInteger(0);
    private boolean zombiesActive;
    private boolean herdingActive;

    private boolean spawnPhase = true;
    private int energyPeak = 0;

    private int combatsPerTurn = 0;
    private int maxCombatsPerTurn = 0;
    private int maxSplurgs = 0;
    private int maxZombies = 0;

    private GameLoop() {}

    public static GameLoop getInstance() {
        return INSTANCE;
    }

    public synchronized void start(boolean zombiesActive, boolean herdingActive) {
        if (started) return;
        this.zombiesActive = zombiesActive;
        this.herdingActive = herdingActive;
        SimulationState.configure(zombiesActive, herdingActive);
        long runGeneration = ++generation;
        running.set(true);
        started = true;
        turn = 0;
        SimulationState.setTurn(turn);
        energyPeak = 0;
        spawnPhase = true;
        maxSplurgs = 0;
        maxCombatsPerTurn = 0;
        maxZombies = 0;
        simulationEngine.resetGuardrails();
        loopSleepTime.set(PropertyHandler.getInt("world.game.loop.sleeptime", 1000));

        loopThread = new Thread(() -> {
            try {
                run(runGeneration);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (runGeneration == generation) {
                    log.error("Game loop interrupted unexpectedly", e);
                }
            } catch (RuntimeException e) {
                log.error("Game loop stopped after an unexpected error", e);
            } finally {
                synchronized (GameLoop.this) {
                    if (runGeneration == generation) {
                        started = false;
                        running.set(false);
                    }
                    if (loopThread == Thread.currentThread()) {
                        loopThread = null;
                    }
                }
            }
        }, "splurg-world-game-loop");

        loopThread.setDaemon(true);
        loopThread.start();
    }

    public synchronized void pause() {
        if (!started) return;
        boolean current = running.get();
        running.set(!current);
        if (!current) {
            log.info("Game unpaused");
        } else {
            log.info("Game paused");
        }
    }


    public synchronized void stop() {
        started = false;
        running.set(false);
        generation++;
        Thread thread = loopThread;
        if (thread != null) {
            thread.interrupt();
        }
        log.info("Game stopping");
    }

    private void run(long runGeneration) throws InterruptedException {
        var loopPauseTime = PropertyHandler.getInt("world.game.loop.pausedelaytime", 1000);

        log.info("Game starting");

        while (started && runGeneration == generation) {
            if (running.get()) {
                turn++;
                SimulationState.setTurn(turn);
                combatsPerTurn = 0;
                WorldFrame.getInstance().refreshStatus();

                WorldPanel worldPanel = WorldFrame.getInstance().getWorldPanel();
                WorldBounds.set(worldPanel.getWorldWidth(), worldPanel.getWorldHeight());
                SimulationFrame frame = simulationEngine.step(
                        turn, combatsPerTurn, WorldFrame.getMaxPopulation());
                SimulationMetrics metrics = frame.metrics();
                worldRenderer.render(worldPanel, frame.snapshot());
                WorldFrame.updateStats(getStats(metrics));

                if (metrics.liveHiveCount() <= 1 || metrics.populationLimitReached()) {
                    WorldFrame.getInstance().displayEndGamePanel(Splurgs.getInstance().getWinningHive());
                }

                Thread.sleep(loopSleepTime.get());
            } else {
                Thread.sleep(loopPauseTime);
            }
        }

        log.info("Game loop has stopped.");
    }

    public void incrementCombatsPerTurn(){
        combatsPerTurn++;
    }

    public int getTurn() {
        return turn;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean isPaused() {
        return !running.get() && started;
    }

    public void setGameSpeed(int delta) {
        int updated = loopSleepTime.addAndGet(delta);
        if (updated < 5) {
            loopSleepTime.set(5);
        } else if (updated > 2000) {
            loopSleepTime.set(2000);
        }
    }

    public void resetGameSpeed() {
        loopSleepTime.set(PropertyHandler.getInt("world.game.loop.sleeptime", 1000));
    }

    private String getStats(SimulationMetrics metrics){
        StringBuilder sb = new StringBuilder("\n");

        sb.append("Turn: ").append(turn).append("\n\n");

        Splurgs splurgs = Splurgs.getInstance();

        List<Hive> hives = Hives.getInstance().getHives();
        Map<Hive, Integer> energy = splurgs.getTotalSplurgEnergyPerHive();
        int totalEnergy = 0;

        for (Hive hive : hives){
            var hiveEnergy = hive.getEnergyReserve();

            if (energy.containsKey(hive)) {
                hiveEnergy += energy.get(hive);
            }

            sb.append(hive.getName())
                    .append(": ")
                    .append(hiveEnergy)
                    .append(" Energy\n");

            totalEnergy += hiveEnergy;
        }
        if (totalEnergy < 10){
            spawnPhase = false;
        }
        if (!spawnPhase && totalEnergy > energyPeak){
            energyPeak = totalEnergy;
        }
        sb.append("Total Energy: ").append(totalEnergy).append(" Energy\n");
        sb.append("Peak Energy: ").append(energyPeak).append(" Energy\n\n");

        if (combatsPerTurn > maxCombatsPerTurn){
            maxCombatsPerTurn = combatsPerTurn;
        }
        sb.append("Combats per Turn: ").append(combatsPerTurn).append("\n");
        sb.append("Max Combats per Turn: ").append(maxCombatsPerTurn).append("\n");

        sb.append("\n");
        sb.append(getSplurgs());
        sb.append("\nDeaths: ").append(splurgs.getDeaths());
        sb.append("\nSpawns: ").append(splurgs.getSpawns());
        sb.append("\nTick time: ").append(metrics.durationNanos() / 1_000_000L).append(" ms");

        sb.append("\n\nAve. Aggression: ").append(splurgs.getAverageSplurgAggression());
        sb.append("\nAve. Foraging: ").append(splurgs.getAverageSplurgForaging());
        sb.append("\nAve. Size: ").append(splurgs.getAverageSplurgSize());
        sb.append("\nAve. Speed: ").append(splurgs.getAverageSplurgSpeed());
        sb.append("\nAve. Strength: ").append(splurgs.getAverageSplurgStrength());
        sb.append("\nAve. Toughness: ").append(splurgs.getAverageSplurgToughness());
        sb.append("\nAve. Charisma: ").append(splurgs.getAverageSplurgCharisma());
        sb.append("\nAve. Loner: ").append(splurgs.getAverageSplurgLoner());

        if (zombiesActive) {
            var zCurrent = splurgs.getZombieCount();
            var zSpawns = splurgs.getZombieSpawns();
            var zDeaths = splurgs.getZombieDeaths();
            if (zCurrent > maxZombies) {
                maxZombies = zCurrent;
            }
            sb.append("\n\nCurrent Zombies: ").append(zCurrent);
            sb.append("\nZombies Spawned: ").append(zSpawns);
            sb.append("\nZombies Killed: ").append(zDeaths);
            sb.append("\nMax Zombies: ").append(maxZombies);
            sb.append("\nInfected: ").append(Splurgs.getTotalInfectedSplurgs());
        }

        return sb.toString();
    }

    public boolean areZombiesActive(){
        return zombiesActive;
    }

    public boolean isHerdingActive() { return herdingActive; }

    private String getSplurgs() {
        Map<Hive, Integer> counts = Splurgs.getSplurgsPerHive();
        StringBuilder stats = new StringBuilder();
        List<Hive> hives = Hives.getInstance().getHives();

        hives.forEach(hive -> {
            if (!stats.isEmpty()) {
                stats.append("\n");
            }
            int count = counts.getOrDefault(hive, 0);
            stats.append(hive.getName()).append(": ").append(count).append(" Splurgs");
        });

        int totalSplurgs = Splurgs.getTotalSplurgs();
        if (totalSplurgs > maxSplurgs){
            maxSplurgs = totalSplurgs;
        }
        stats.append("\nLive Splurgs: ").append(totalSplurgs);
        stats.append("\nMax Live Splurgs: ").append(maxSplurgs);

        return stats.toString();
    }
}
