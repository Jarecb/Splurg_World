package org.jarec.data;

import org.jarec.data.creature.Splurg;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class NestTest {
    private static final Nest testNest = new Nest(new Location(10, 20), Color.RED, "The Hive");

    @Test
    void getFood() {
        testNest.setFoodReserve(10);
        assertEquals(5, testNest.getFood(5), "Failed to get food requested");
        assertEquals(5, testNest.getFood(6), "Failed to get max food available");
        assertEquals(0, testNest.getFoodReserve(), "Food adjustment failed");
    }

    @Test
    void addFood() {
        testNest.setFoodReserve(20);
        testNest.addFood(5);
        assertEquals(25, testNest.getFoodReserve());
    }
}