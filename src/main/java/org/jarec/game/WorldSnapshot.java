package org.jarec.game;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Render data for one world state. The entity lists are defensively copied; image icons are
 * retained by reference because they are shared rendering resources.
 */
public record WorldSnapshot(List<HiveView> hives, List<SplurgView> splurgs) {
    public WorldSnapshot {
        hives = List.copyOf(hives);
        splurgs = List.copyOf(splurgs);
    }

    public record HiveView(int x, int y, Color color, BufferedImage icon, int energy, int population) {}

    public record SplurgView(int x, int y, int size, Color color) {}
}
