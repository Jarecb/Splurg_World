package org.jarec.data;

import java.util.concurrent.atomic.AtomicReference;

public final class WorldBounds {
    private static final AtomicReference<Size> size = new AtomicReference<>(new Size(900, 700));

    private WorldBounds() {}

    public static void set(int worldWidth, int worldHeight) {
        if (worldWidth < 1 || worldHeight < 1) {
            throw new IllegalArgumentException("World dimensions must be positive");
        }
        size.set(new Size(worldWidth, worldHeight));
    }

    public static int getWidth() {
        return size.get().width();
    }

    public static int getHeight() {
        return size.get().height();
    }

    public static Size getSize() {
        return size.get();
    }

    public record Size(int width, int height) {}
}
