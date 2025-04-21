package org.jarec.data.creature;

import org.jarec.data.Hive;
import org.jarec.data.Location;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class SplurgTest {

    private static final Hive TEST_HIVE = new Hive(new Location(0, 0), Color.RED, "The Hive", false);

    @Test
    void getAge() {
        Splurg splurg = new Splurg(TEST_HIVE);
        splurg.setAge(100);
        Splurg splurg2 = new Splurg(TEST_HIVE);
        splurg2.setAge(50);
        assertEquals(100, splurg.getAge());
        assertEquals(50, splurg2.getAge());
    }

    @Test
    void health() {
        Splurg splurg = new Splurg(TEST_HIVE);
        splurg.setMaxHealth(20);
        splurg.setHealth(20);
        assertTrue(splurg.reduceHealth(15));
        assertEquals(5, splurg.getHealth());
        splurg.setHealth(20);
        assertFalse(splurg.reduceHealth(20));
        assertEquals(0, splurg.getHealth());
        splurg.setHealth(21);
        assertEquals(20, splurg.getHealth());
        splurg.reduceHealth(10);
        // TODO Fix this test
//        splurg.recoverHealth(11);
//        assertEquals(20, splurg.getHealth());
    }

    @Test
    void getSize() {
        Splurg splurg = new Splurg(TEST_HIVE);
        int expected = (splurg.getToughness().getValue() +
                splurg.getStrength().getValue()) / 2;
        assertEquals(expected, splurg.getSize().getValue());
    }

    @Test
    void getSpeed() {
        Splurg splurg = new Splurg(TEST_HIVE);
        int expected = 10 - splurg.getSize().getValue();
        assertEquals(expected, splurg.getSpeed().getValue());
    }

    @Test
    void spawnNewSplurgWithMatchingAttributes() {
        Splurg parent1 = new Splurg(TEST_HIVE);
        parent1.setAggression(5);
        parent1.setForaging(7);
        parent1.setStrength(3);
        parent1.setToughness(10);
        Splurg parent2 = new Splurg(TEST_HIVE);
        parent2.setAggression(5);
        parent2.setForaging(7);
        parent2.setStrength(3);
        parent2.setToughness(10);

        Splurg splurgSpawn = new Splurg(parent1, parent2);

        assertEquals(5, splurgSpawn.getAggression().getValue(), "Aggression not cloned");
        assertEquals(7, splurgSpawn.getForaging().getValue(), "Foraging not cloned");
        assertEquals(3, splurgSpawn.getStrength().getValue(), "Strength not cloned");
        assertEquals(10, splurgSpawn.getToughness().getValue(), "Toughness not cloned");
    }
}