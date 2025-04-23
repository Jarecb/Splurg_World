package org.jarec.game;

import org.jarec.data.Hive;
import org.jarec.game.resources.Hives;
import org.jarec.game.resources.Splurgs;
import org.jarec.gui.WorldFrame;
import org.jarec.gui.WorldPanel;
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

    private int turn = 0;
    private AtomicBoolean running = new AtomicBoolean(false);
    private volatile boolean started = false;
    private final AtomicInteger loopSleepTime = new AtomicInteger(0);
    private boolean zombiesActive;

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

    public void start(boolean zombiesActive) {
        if (started) return;
        this.zombiesActive = zombiesActive;
        running = new AtomicBoolean(true);
        started = true;
        turn = 0;
        energyPeak = 0;
        spawnPhase = true;
        maxSplurgs = 0;
        maxCombatsPerTurn = 0;
        maxZombies = 0;
        loopSleepTime.set(Integer.parseInt(PropertyHandler.get("world.game.loop.sleeptime", "1000")));

        Thread loopThread = new Thread(() -> {
            try {
                run();
            } catch (InterruptedException e) {
                log.error("Something went wrong in the game loop", e);
                Thread.currentThread().interrupt();
            }
        });

        loopThread.setDaemon(true);
        loopThread.start();
    }

    public void pause() {
        boolean current = running.get();
        running.set(!current);
        if (!current) {
            log.info("Game unpaused");
        } else {
            log.info("Game paused");
        }
    }


    public void stop() {
        started = false;
        log.info("Game stopping");
    }

    private void run() throws InterruptedException {
        var loopPauseTime = Integer.parseInt(PropertyHandler.get("world.game.loop.pausedelaytime", "1000"));

        log.info("Game starting");

        while (started) {
            if (running.get()) {
                turn++;
                combatsPerTurn = 0;
                WorldFrame.getInstance().refreshStatus();

                Splurgs.getInstance().reorder();

                Splurgs.getInstance().drawSplurges();
                Splurgs.getInstance().moveSplurgs();
                Splurgs.getInstance().removeDeadSplurgs();
                Splurgs.getInstance().healSplurgs();
                Splurgs.getInstance().depositEnergy();
                Splurgs.getInstance().handleBreeding();

                Hives.getInstance().drawHives();
                Hives.getInstance().spawnHives();

                WorldPanel worldPanel = WorldFrame.getInstance().getWorldPanel();
                worldPanel.publish();

                WorldFrame.updateStats(getStats());

                // Endgame check
                if(Splurgs.getLiveHiveCount() <= 1 || Splurgs.getTotalSplurgs() > WorldFrame.getMaxPopulation()) {
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
        loopSleepTime.set(Integer.parseInt(PropertyHandler.get("world.game.loop.sleeptime", "1000")));
    }

    private String getStats(){
        // TODO Adjust stats for zombies

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

        sb.append("\n\nAve. Aggression: ").append(splurgs.getAverageSplurgAggression());
        sb.append("\nAve. Foraging: ").append(splurgs.getAverageSplurgForaging());
        sb.append("\nAve. Size: ").append(splurgs.getAverageSplurgSize());
        sb.append("\nAve. Speed: ").append(splurgs.getAverageSplurgSpeed());
        sb.append("\nAve. Strength: ").append(splurgs.getAverageSplurgStrength());
        sb.append("\nAve. Toughness: ").append(splurgs.getAverageSplurgToughness());

        if (zombiesActive) {
            var zSpawns = splurgs.getZombieSpawns();
            var zDeaths = splurgs.getZombieDeaths();
            var zCurrent = splurgs.getZombieCount();
            if (zCurrent > maxZombies) {
                maxZombies++;
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

    private String getSplurgs() {
        Map<Hive, Integer> counts = Splurgs.getSplurgsPerHive();
        StringBuilder stats = new StringBuilder();
        List<Hive> hives = Hives.getInstance().getHives();

        hives.forEach(hive -> {
            if (stats.length() > 0) {
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
