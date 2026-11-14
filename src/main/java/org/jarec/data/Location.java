package org.jarec.data;

public class Location {
    private int x;
    private int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public synchronized int getX() {
        return x;
    }

    public synchronized int getY() {
        return y;
    }

    public synchronized void rescale(int oldWidth, int oldHeight, int newWidth, int newHeight) {
        if (oldWidth < 1 || oldHeight < 1 || newWidth < 1 || newHeight < 1) {
            throw new IllegalArgumentException("World dimensions must be positive");
        }
        x = rescaleCoordinate(x, oldWidth, newWidth);
        y = rescaleCoordinate(y, oldHeight, newHeight);
    }

    private int rescaleCoordinate(int coordinate, int oldSize, int newSize) {
        if (newSize == 1) return 0;
        if (oldSize == 1) return (newSize - 1) / 2;
        long scaled = Math.round((double) coordinate * (newSize - 1) / (oldSize - 1));
        return (int) Math.clamp(scaled, 0L, newSize - 1L);
    }

    /**
     * Moves one step using the configured world bounds, reflecting the heading at an edge.
     *
     * @return the resulting heading, which may change when an edge is reached
     */
    public Heading updateLocation(Heading heading) {
        WorldBounds.Size size = WorldBounds.getSize();
        return updateLocation(heading, size.width(), size.height());
    }

    /**
     * Moves one step within the supplied dimensions, reflecting each heading component that
     * would cross an edge. Coordinates are expected to start inside the world.
     *
     * @return the resulting heading, which may change when an edge is reached
     * @throws IllegalArgumentException if either world dimension is not positive
     */
    public synchronized Heading updateLocation(Heading heading, int worldWidth, int worldHeight) {
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
    public synchronized String toString() {
        return String.format(
                "{\"x\": %d, \"y\": %d}",
                x,
                y
        );
    }
}
