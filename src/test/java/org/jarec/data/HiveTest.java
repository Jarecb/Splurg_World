package org.jarec.data;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class HiveTest {
    private static final Hive TEST_HIVE = new Hive(new Location(10, 20), Color.RED, "The Hive");

    @Test
    void getEnergy() {
        TEST_HIVE.setEnergyReserve(10);
        assertEquals(5, TEST_HIVE.getEnergy(5), "Failed to get energy requested");
        assertEquals(5, TEST_HIVE.getEnergy(6), "Failed to get max energy available");
        assertEquals(0, TEST_HIVE.getEnergyReserve(), "Energy adjustment failed");
    }

    @Test
    void addEnergy() {
        TEST_HIVE.setEnergyReserve(20);
        TEST_HIVE.addEnergy(5);
        assertEquals(25, TEST_HIVE.getEnergyReserve());
    }
}