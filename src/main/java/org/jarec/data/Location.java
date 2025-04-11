package org.jarec.data;

import org.jarec.gui.WorldFrame;
import org.jarec.gui.WorldPanel;

public class Location {
    private int x;
    private int y;

    public Location(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Heading updateLocation(Heading heading) {
        var worldWidth = WorldFrame.getInstance().getWorldPanel().getWorldWidth();
        var worldHeight = WorldFrame.getInstance().getWorldPanel().getWorldHeight();

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

        // If direction changed, return new heading
        if (dx != heading.getDx() || dy != heading.getDy()) {
            return HeadingUtils.getHeadingFromVector(dx, dy);
        }

        return heading;
    }

}
