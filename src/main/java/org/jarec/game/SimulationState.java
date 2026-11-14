package org.jarec.game;

public final class SimulationState {
    private static volatile int turn;
    private static volatile boolean zombiesActive;
    private static volatile boolean herdingActive;

    private SimulationState() {}

    public static void configure(boolean zombies, boolean herding) {
        zombiesActive = zombies;
        herdingActive = herding;
    }

    public static void setTurn(int currentTurn) {
        turn = currentTurn;
    }

    public static int getTurn() {
        return turn;
    }

    public static boolean areZombiesActive() {
        return zombiesActive;
    }

    public static boolean isHerdingActive() {
        return herdingActive;
    }
}
