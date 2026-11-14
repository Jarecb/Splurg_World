package org.jarec.game;

public record SimulationMetrics(
        int turn,
        int splurgCount,
        int liveHiveCount,
        int zombieCount,
        int totalEnergy,
        int deaths,
        int spawns,
        int combats,
        long durationNanos,
        boolean populationLimitReached,
        boolean slowTick
) {}
