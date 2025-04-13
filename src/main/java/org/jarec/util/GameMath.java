package org.jarec.util;

import org.jarec.data.Location;

public class GameMath {

    public static double calculateHypotenuse(Location from, Location to) {
        double dx = from.getX() - to.getX();
        double dy = from.getY() - to.getY();
        return java.lang.Math.sqrt(dx * dx + dy * dy);
    }
}
