package org.jarec.data;

public class Location {
    private int x;
    private int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Moves one step using the configured world bounds, reflecting the heading at an edge.
     *
     * @return the resulting heading, which may change when an edge is reached
     */
    public Heading updateLocation(Heading heading) {
        return updateLocation(heading, WorldBounds.getWidth(), WorldBounds.getHeight());
    }

    /**
     * Moves one step within the supplied dimensions, reflecting each heading component that
     * would cross an edge. Coordinates are expected to start inside the world.
     *
     * @return the resulting heading, which may change when an edge is reached
     * @throws IllegalArgumentException if either world dimension is not positive
     */
    public Heading updateLocation(Heading heading, int worldWidth, int worldHeight) {
        if (worldWidth < 1 || worldHeight < 1) {
            throw new IllegalArgumentException("World dimensions must be positive");
        }

        int newX = this.x + heading.getDx();
        int newY = this.y + heading.getDy();

        int dx = heading.getDx();
        int dy = heading.getDy();

        boolean outOfBoundsX = newX < 0 || newX >= worldWidth;
        boolean outOfBoundsY = newY < 0 || newY >= worldHeight;

        if (outOfBoundsX) dx = -dx;
        if (outOfBoundsY) dy = -dy;

        this.x += dx;
        this.y += dy;

        if (dx != heading.getDx() || dy != heading.getDy()) {
            return HeadingUtils.getHeadingFromVector(dx, dy);
        }

        return heading;
    }

    @Override
    public String toString() {
        return String.format(
                "{\"x\": %d, \"y\": %d}",
                x,
                y
        );
    }
}
