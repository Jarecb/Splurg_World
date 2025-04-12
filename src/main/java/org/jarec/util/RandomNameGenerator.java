package org.jarec.util;

import java.util.Random;

public class RandomNameGenerator {
    // Arrays of possible prefixes and suffixes for creating fantasy names
    private static final String[] PREFIXES = {
            "Al", "Bri", "Eri", "Fen", "Gal", "Hel", "Ith", "Kri", "Lau", "Mor", "Nor", "Pha", "Riv", "Syl", "Thal", "Val"
    };

    private static final String[] SUFFIXES = {
            "ar", "an", "or", "ith", "arion", "iel", "ian", "rath", "oth", "da", "os", "orath", "riel", "rius", "gar"
    };

    private static final String[] SECOND_PARTS = {
            "dar", "dra", "nor", "dir", "wen", "nor", "lian", "mir", "thar", "ven", "vian", "el", "thos", "vor", "ath"
    };

    // Random object for generating random indices
    private static final Random RANDOM = new Random();

    // Method to generate a random fantasy name
    public static String generateName() {
        // Randomly choose a prefix and suffix
        String prefix = PREFIXES[RANDOM.nextInt(PREFIXES.length)];
        String suffix = SUFFIXES[RANDOM.nextInt(SUFFIXES.length)];
        String secondPart = SECOND_PARTS[RANDOM.nextInt(SECOND_PARTS.length)];

        // Combine them to form a name
        return prefix + secondPart + suffix;
    }
}
