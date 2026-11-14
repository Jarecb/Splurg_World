package org.jarec.game;

public record SimulationFrame(SimulationMetrics metrics, WorldSnapshot snapshot) {}
