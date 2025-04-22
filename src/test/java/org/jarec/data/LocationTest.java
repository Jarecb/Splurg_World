package org.jarec.data;

import org.jarec.gui.WorldFrame;
import org.jarec.gui.WorldPanel;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocationTest {

    @Test
    void updateLocation() {
        WorldPanel mockPanel = mock(WorldPanel.class);
        when(mockPanel.getWorldWidth()).thenReturn(200);
        when(mockPanel.getWorldHeight()).thenReturn(200);

        try (MockedStatic<WorldFrame> mockedStatic = mockStatic(WorldFrame.class)) {
            WorldFrame mockFrame = mock(WorldFrame.class);
            when(mockFrame.getWorldPanel()).thenReturn(mockPanel);
            mockedStatic.when(WorldFrame::getInstance).thenReturn(mockFrame);

            Location testLocation = new Location(100, 100);
            testLocation.updateLocation(Heading.NORTH_WEST); // -1, 1
            assertEquals(99, testLocation.getX());
            assertEquals(101, testLocation.getY());

            testLocation.updateLocation(Heading.SOUTH_EAST); // 1, -1
            assertEquals(100, testLocation.getX());
            assertEquals(100, testLocation.getY());
        }
    }
}