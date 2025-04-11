package org.jarec.game;

import org.jarec.game.resources.Nests;
import org.jarec.game.resources.Splurgs;
import org.jarec.gui.WorldFrame;
import org.jarec.gui.WorldPanel;
import org.jarec.util.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameLoop {
    private static final Logger log = LoggerFactory.getLogger(GameLoop.class);

    private static final GameLoop instance = new GameLoop();

    private int turn = 0;
    private volatile boolean running = false;
    private volatile boolean started = false;

    private GameLoop() {}

    public static GameLoop getInstance() {
        return instance;
    }

    public void start() {
        if (started) return;
        running = true;
        started = true;
        turn = 0;

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
        var loopSleepTime = Integer.parseInt(PropertyHandler.get("world.game.loop.sleeptime", "1000"));
        var loopPauseTime = Integer.parseInt(PropertyHandler.get("world.game.loop.pausedelaytime", "1000"));

        log.info("Game starting");

        while (started) {
            if (running) {
                turn++;
                WorldFrame.getInstance().updateStatus("");

                Splurgs.getInstance().drawSplurges();
                Splurgs.getInstance().moveSplurgs();
                Splurgs.getInstance().removeDeadSplurgs();

                Nests.getInstance().drawNests();
                Nests.getInstance().spawnNests();

                WorldPanel worldPanel = WorldFrame.getInstance().getWorldPanel();
                worldPanel.publish();

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
}
