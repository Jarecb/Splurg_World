package org.jarec.data;

public class HeadingUtils {
    private HeadingUtils() {
    }

    public static Heading getHeadingFromVector(int dx, int dy) {
        for (Heading h : Heading.values()) {
            if (h.getDx() == dx && h.getDy() == dy) {
                return h;
            }
        }
        return null;
    }

    public static Heading getHeadingTo(Location from, Location to) {
        int dx = Integer.compare(to.getX() - from.getX(), 0);
        int dy = Integer.compare(to.getY() - from.getY(), 0);

        if (dx == 0 && dy == 0) {
            return null;
        }

        if (dx == 0) return dy > 0 ? Heading.NORTH : Heading.SOUTH;
        if (dy == 0) return dx > 0 ? Heading.EAST : Heading.WEST;
        if (dx > 0) return dy > 0 ? Heading.NORTH_EAST : Heading.SOUTH_EAST;
        return dy > 0 ? Heading.NORTH_WEST : Heading.SOUTH_WEST;
    }
}
