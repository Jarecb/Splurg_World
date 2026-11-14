package org.jarec.game;

import org.jarec.data.Hive;
import org.jarec.data.Location;
import org.jarec.data.WorldBounds;
import org.jarec.data.creature.Splurg;
import org.jarec.game.resources.Hives;
import org.jarec.game.resources.Splurgs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameLoopResizeTest {
    @AfterEach
    void clearWorld() {
        Splurgs.getInstance().clearSplurgs();
        Hives.getInstance().clearHives();
        WorldBounds.set(900, 700);
    }

    @Test
    void resizingScalesHivesAndSplurgsAndClampsSplurgsToTheNewEdges() {
        WorldBounds.set(100, 100);
        Hive hive = new Hive(new Location(75, 50), Color.RED, "Red Hive", false);
        Hives.addHive(hive);
        Splurg splurg = new Splurg(hive);
        splurg.setLocation(new Location(150, -5));

        GameLoop.getInstance().resizeWorld(40, 20);

        assertEquals(30, hive.getLocation().getX());
        assertEquals(10, hive.getLocation().getY());
        assertEquals(39, splurg.getLocation().getX());
        assertEquals(0, splurg.getLocation().getY());
        assertEquals(40, WorldBounds.getWidth());
        assertEquals(20, WorldBounds.getHeight());
    }
}
