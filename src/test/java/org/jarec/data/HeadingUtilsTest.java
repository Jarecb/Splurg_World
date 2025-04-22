package org.jarec.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeadingUtilsTest {

    @Test
    void getHeadingFromVector() {
        assertEquals(Heading.SOUTH_EAST, HeadingUtils.getHeadingFromVector(1, -1));
    }

    @Test
    void getHeadingTo() {
        Location locationFrom = new Location(100, 100);
        Location locationTo = new Location(90, 110);

        assertEquals(Heading.NORTH_WEST, HeadingUtils.getHeadingTo(locationFrom, locationTo));
    }
}