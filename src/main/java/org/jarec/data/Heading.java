package org.jarec.data;

import org.jarec.util.RandomInt;

public enum Heading {
    NORTH(0, 1),
    NORTH_EAST(1, 1),
    EAST(1, 0),
    SOUTH_EAST(1, -1),
    SOUTH(0, -1),
    SOUTH_WEST(-1, -1),
    WEST(-1, 0),
    NORTH_WEST(-1, 1);

    private final int dx;
    private final int dy;
    private static final Heading[] VALUES = values();
    private static final int SIZE = VALUES.length;

    Heading(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public int[] getVector() {
        return new int[]{dx, dy};
    }

    public static Heading getRandomHeading() {
        return VALUES[RandomInt.getRandomInt(SIZE) - 1];
    }

    public Heading getRandomTurn() {
        int direction = RandomInt.getRandomBoolean() ? 1 : -1;
        int newIndex = (this.ordinal() + direction + SIZE) % SIZE;
        return VALUES[newIndex];
    }

    @Override
    public String toString() {
        return String.format("%s(%d, %d)", name(), dx, dy);
    }
}
