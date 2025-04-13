package org.jarec.data;

public class HeadingUtils {
    public static Heading getHeadingFromVector(int dx, int dy) {
        for (Heading h : Heading.values()) {
            if (h.getDx() == dx && h.getDy() == dy) {
                return h;
            }
        }
        return null;
    }

    public static Heading getHeadingTo(Location from, Location to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();

        if (dx == 0 && dy == 0) {
            return null;
        }

        int dxDirection = Integer.compare(dx, 0);  // -1 for left, 1 for right, 0 for no move
        int dyDirection = Integer.compare(dy, 0);  // -1 for down, 1 for up, 0 for no move

        // Use the dx and dy directions to select the right heading
        if (dxDirection == 0) {
            if (dyDirection > 0) {
                return Heading.NORTH;
            } else {
                return Heading.SOUTH;
            }
        } else if (dyDirection == 0) {
            if (dxDirection > 0) {
                return Heading.EAST;
            } else {
                return Heading.WEST;
            }
        } else {
            // Determine the diagonal heading
            if (dxDirection > 0) {
                if (dyDirection > 0) {
                    return Heading.NORTH_EAST;
                } else {
                    return Heading.SOUTH_EAST;
                }
            } else {
                if (dyDirection > 0) {
                    return Heading.NORTH_WEST;
                } else {
                    return Heading.SOUTH_WEST;
                }
            }
        }
    }
}
