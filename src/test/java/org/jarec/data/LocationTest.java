package org.jarec.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

    @Test
    void updateLocation() {
        Location testLocation = new Location(100, 100);
        testLocation.updateLocation(Heading.NORTH_WEST, 200, 200);
        assertEquals(99, testLocation.getX());
        assertEquals(101, testLocation.getY());

        testLocation.updateLocation(Heading.SOUTH_EAST, 200, 200);
        assertEquals(100, testLocation.getX());
        assertEquals(100, testLocation.getY());
    }
}