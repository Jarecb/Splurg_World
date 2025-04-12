package org.jarec.game;

import org.jarec.data.Nest;
import org.jarec.game.resources.Nests;
import org.jarec.game.resources.Splurgs;
import org.jarec.gui.WorldFrame;
import org.jarec.gui.WorldPanel;
import org.jarec.util.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GameLoop {
    private static final Logger log = LoggerFactory.getLogger(GameLoop.class);

    private static final GameLoop instance = new GameLoop();

    private int turn = 0;
    private volatile boolean running = false;
    private volatile boolean started = false;
    private volatile int loopSleepTime = 0;

    private GameLoop() {}

    public static GameLoop getInstance() {
        return instance;
    }

    public void start() {
        if (started) return;
        running = true;
        started = true;
        turn = 0;
        loopSleepTime = Integer.parseInt(PropertyHandler.get("world.game.loop.sleeptime", "1000"));

        Thread loopThread = new Thread(() -> {
            try {
                run();
            } catch (InterruptedException e) {
                log.error("Something went wrong in the game loop: {}", e);
                Thread.currentThread().interrupt();
            }
        });

        loopThread.setDaemon(true);
        loopThread.start();
    }

    public void pause() {
        running = !running;
        if (running) {
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
            if (running) {
                turn++;
                WorldFrame.getInstance().refreshStatus();

                Splurgs.getInstance().drawSplurges();
                Splurgs.getInstance().moveSplurgs();
                Splurgs.getInstance().removeDeadSplurgs();
                Splurgs.getInstance().healSplurgs();
                Splurgs.getInstance().depositEnergy();
                Splurgs.getInstance().handleBreeding();

                Nests.getInstance().drawNests();
                Nests.getInstance().spawnNests();

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
        return running;
    }

    public boolean isPaused() {
        return !running && started;
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

        Map<Nest, Integer> energy = splurgs.getTotalEnergyPerNest();
        int totalEnergy = 0; // Variable to store the total energy

        for (Map.Entry<Nest, Integer> entry : energy.entrySet()) {
            sb.append(entry.getKey().getName())
                    .append(": ")
                    .append(entry.getValue())
                    .append(" Energy\n");

            // Add each nest's energy to the total energy
            totalEnergy += entry.getValue();
        }
        sb.append("Total Energy: ").append(totalEnergy).append(" Energy\n");

        sb.append("\n");
        sb.append(getSplurgs());

        sb.append("\n\nAve. Aggression: ").append(splurgs.getAverageSplurgAggression());
        sb.append("\nAve. Strength: ").append(splurgs.getAverageSplurgStrength());
        sb.append("\nAve. Toughness: ").append(splurgs.getAverageSplurgToughness());
        sb.append("\nAve. Speed: ").append(splurgs.getAverageSplurgSpeed());
        sb.append("\nAve. Size: ").append(splurgs.getAverageSplurgSize());

        return sb.toString();
    }

    private String getSplurgs() {
        Map<Nest, Long> counts = Splurgs.getInstance().getCounts();
        StringBuilder stats = new StringBuilder();

        counts.forEach((nest, count) -> {
            if (stats.length() > 0) {
                stats.append("\n");
            }
            stats.append(nest.getName()).append(": ").append(count).append(" Splurgs");
        });
        long totalSplurgs = counts.values().stream().mapToLong(Long::longValue).sum();
        stats.append("\nTotal Splurgs: ").append(totalSplurgs);

        return stats.toString();
    }

}
