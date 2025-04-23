package org.jarec.game;

import org.jarec.data.Hive;
import org.jarec.data.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameEndCheckTest {

    @Test
    void redWins() {
        Hive hive = new Hive(new Location(0, 0), null, "Red Hive", false);
        assertEquals(GameEndState.RED, GameEndCheck.determineEndState(hive));
    }

    @Test
    void blueWins() {
        Hive hive = new Hive(new Location(0, 0), null, "Blue Hive", false);
        assertEquals(GameEndState.BLUE, GameEndCheck.determineEndState(hive));
    }

    @Test
    void greenWins() {
        Hive hive = new Hive(new Location(0, 0), null, "Green Hive", false);
        assertEquals(GameEndState.GREEN, GameEndCheck.determineEndState(hive));
    }

    @Test
    void yellowWins() {
        Hive hive = new Hive(new Location(0, 0), null, "Yellow Hive", false);
        assertEquals(GameEndState.YELLOW, GameEndCheck.determineEndState(hive));
    }

    @Test
    void truceWins() {
        assertEquals(GameEndState.STALEMATE, GameEndCheck.determineEndState(null));
    }

    @Test
    void zombieWins() {
        Hive hive = new Hive(new Location(0, 0), null, "Yellow Hive", true);
        assertEquals(GameEndState.ZOMBIE, GameEndCheck.determineEndState(hive));
    }
}