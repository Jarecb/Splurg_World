package org.jarec.game.resources;

import org.jarec.data.Hive;
import org.jarec.data.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HivesTest {

    @BeforeEach
    void setUp() {
        Hives.getInstance().clearHives();
    }

    @Test
    void getInstance() {
        assertInstanceOf(Hives.class, Hives.getInstance());
    }

    @Test
    void getZombieHiveWhenOneExists() {
        Hive zombieHive = new Hive(new Location(0, 0), null, "zombie", true);
        Hives.addHive(zombieHive);
        assertInstanceOf(Hive.class, Hives.getZombieHive());
        assertEquals(zombieHive, Hives.getZombieHive());
    }

    @Test
    void getZombieHiveWhenNoneExists() {
        assertNull(Hives.getZombieHive());
    }

    @Test
    void addHive() {
        Hive hive = new Hive(new Location(0, 0), null, "Hive", false);
        Hives.addHive(hive);
        List<Hive> hiveList = Hives.getInstance().getHives();
        assertEquals(1, hiveList.size());
        assertTrue(hiveList.contains(hive));
        assertNull(Hives.getZombieHive());
    }

    @Test
    void addZombieHive() {
        Hive zombieHive = new Hive(new Location(0, 0), null, "Hive", true);
        Hives.addHive(zombieHive);
        assertEquals(zombieHive, Hives.getZombieHive());
    }

    @Test
    void getHives() {
        var result = Hives.getInstance().getHives();
        assertTrue(result.isEmpty());
        Hive hiveOne = new Hive(new Location(0,0), null, "Hive one", false);
        Hive hiveTwo = new Hive(new Location(0,0), null, "Hive two", false);
        Hives.addHive(hiveOne);
        Hives.addHive(hiveTwo);
        result = Hives.getInstance().getHives();
        assertEquals(2, result.size());
        assertTrue(result.contains(hiveOne));
        assertTrue(result.contains(hiveTwo));
    }

    @Test
    void getHiveCount() {
        Hive hiveOne = new Hive(new Location(0, 0), null, "Hive", false);
        Hive hiveTwo = new Hive(new Location(0, 0), null, "Hive", false);
        Hive hiveThree = new Hive(new Location(0, 0), null, "Hive", false);
        Hives.addHive(hiveOne);
        Hives.addHive(hiveTwo);
        Hives.addHive(hiveThree);
        assertEquals(3, Hives.getInstance().getHiveCount());
    }
}