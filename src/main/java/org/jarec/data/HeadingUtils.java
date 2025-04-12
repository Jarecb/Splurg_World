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

    public static Heading getHeadingTo(Location fromLocation, Location toLocation) {
        int dx = toLocation.getX() - fromLocation.getX();
        int dy = toLocation.getY() - fromLocation.getY();

        // Normalize dx and dy to be in the range of [-1, 0, 1]
        int headingDx = Integer.compare(dx, 0);  // -1, 0, or 1
        int headingDy = Integer.compare(dy, 0);  // -1, 0, or 1

        // Use getHeadingFromVector to return the corresponding Heading
        return getHeadingFromVector(headingDx, headingDy);
    }
}
