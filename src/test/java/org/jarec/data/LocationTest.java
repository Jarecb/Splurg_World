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

    @Test
    void rescaleClampsCoordinatesToResizedWorld() {
        Location location = new Location(150, -5);

        location.rescale(100, 100, 40, 20);

        assertEquals(39, location.getX());
        assertEquals(0, location.getY());
    }

    @Test
    void rescaleFromSinglePixelWorldKeepsPositionCentered() {
        Location location = new Location(0, 0);

        location.rescale(1, 1, 10, 8);

        assertEquals(4, location.getX());
        assertEquals(3, location.getY());
    }
}