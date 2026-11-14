package org.jarec.data;

public final class WorldBounds {
    private static volatile int width = 900;
    private static volatile int height = 700;

    private WorldBounds() {}

    public static void set(int worldWidth, int worldHeight) {
        if (worldWidth < 1 || worldHeight < 1) {
            throw new IllegalArgumentException("World dimensions must be positive");
        }
        width = worldWidth;
        height = worldHeight;
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }
}
