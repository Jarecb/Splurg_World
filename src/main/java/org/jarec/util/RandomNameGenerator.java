package org.jarec.util;

import java.util.Random;

public class RandomNameGenerator {
    private RandomNameGenerator() {
    }

    private static final String[] PREFIXES = {
            "Al", "Bri", "Eri", "Fen", "Gal", "Hel", "Ith", "Kri", "Lau", "Mor", "Nor", "Pha", "Riv", "Syl", "Thal", "Val"
    };

    private static final String[] SUFFIXES = {
            "ar", "an", "or", "ith", "arion", "iel", "ian", "rath", "oth", "da", "os", "orath", "riel", "rius", "gar"
    };

    private static final String[] SECOND_PARTS = {
            "dar", "dra", "nor", "dir", "wen", "nor", "lian", "mir", "thar", "ven", "vian", "el", "thos", "vor", "ath"
    };

    private static final Random RANDOM = new Random();

    // Method to generate a random name, useful for log reading
    public static String generateName() {
        String prefix = PREFIXES[RANDOM.nextInt(PREFIXES.length)];
        String suffix = SUFFIXES[RANDOM.nextInt(SUFFIXES.length)];
        String secondPart = SECOND_PARTS[RANDOM.nextInt(SECOND_PARTS.length)];

        return prefix + secondPart + suffix;
    }
}
