package org.jarec.util;

import org.jarec.data.Heading;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RandomIntTest {
    @Test
    void seededRandomnessReproducesSimulationChoices() {
        String firstRun = runChoices(123456L);
        String repeatedRun = runChoices(123456L);

        assertEquals(firstRun, repeatedRun);
    }

    private String runChoices(long seed) {
        RandomInt.setSeed(seed);
        List<Integer> shuffled = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        RandomInt.shuffle(shuffled);
        return Heading.getRandomHeading() + ":" + RandomNameGenerator.generateName() + ":" + shuffled;
    }
}
