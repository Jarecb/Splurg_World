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

public class GameLoop {
    private static final Logger log = LoggerFactory.getLogger(GameLoop.class);

    private static final GameLoop INSTANCE = new GameLoop();

    private int turn = 0;
    private AtomicBoolean running = new AtomicBoolean(false);
    private volatile boolean started = false;
    private volatile int loopSleepTime = 0;

    private GameLoop() {}

    public static GameLoop getInstance() {
        return INSTANCE;
    }

    public void start() {
        if (started) return;
        running = new AtomicBoolean(true);
        started = true;
        turn = 0;
        loopSleepTime = Integer.parseInt(PropertyHandler.get("world.game.loop.sleeptime", "1000"));

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

                Thread.sleep(loopSleepTime);
            } else {
                Thread.sleep(loopPauseTime);
            }
        }

        log.info("Game loop has stopped.");
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

    public void setGameSpeed(int loopSleepTime) {
        this.loopSleepTime += loopSleepTime;
        if (this.loopSleepTime < 5) {
            this.loopSleepTime = 5;
        } else if (this.loopSleepTime > 2000) {
            this.loopSleepTime = 2000;
        }
    }

    public void resetGameSpeed() {
        loopSleepTime = Integer.parseInt(PropertyHandler.get("world.game.loop.sleeptime", "1000"));
    }



    private String getStats(){
        StringBuilder sb = new StringBuilder("\n");

        Splurgs splurgs = Splurgs.getInstance();

        List<Hive> hives = Hives.getInstance().getHives();
        Map<Hive, Integer> energy = splurgs.getTotalEnergyPerHive();
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
        sb.append("Total Energy: ").append(totalEnergy).append(" Energy\n");

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

        return sb.toString();
    }

    private String getSplurgs() {
        Map<Hive, Long> counts = Splurgs.getInstance().getCounts();
        StringBuilder stats = new StringBuilder();
        List<Hive> hives = Hives.getInstance().getHives();

        hives.forEach(hive -> {
            if (stats.length() > 0) {
                stats.append("\n");
            }
            // Get the count for the current hive, defaulting to 0 if not found
            long count = counts.getOrDefault(hive, 0L);
            stats.append(hive.getName()).append(": ").append(count).append(" Splurgs");
        });

        long totalSplurgs = counts.values().stream().mapToLong(Long::longValue).sum();
        stats.append("\nLive Splurgs: ").append(totalSplurgs);

        return stats.toString();
    }

}
