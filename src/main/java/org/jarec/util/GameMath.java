package org.jarec.util;

import org.jarec.data.Location;

public class GameMath {
    private GameMath() {}

    public static int calculateHypotenuse(Location from, Location to) {
        double dx = from.getX() - (double)to.getX();
        double dy = from.getY() - (double)to.getY();
        return (int)java.lang.Math.sqrt(dx * dx + dy * dy);
    }
}
