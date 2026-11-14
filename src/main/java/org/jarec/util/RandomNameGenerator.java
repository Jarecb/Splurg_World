package org.jarec.util;

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

    // Method to generate a random name, useful for log reading
    public static String generateName() {
        String prefix = PREFIXES[RandomInt.getRandomInt(PREFIXES.length) - 1];
        String suffix = SUFFIXES[RandomInt.getRandomInt(SUFFIXES.length) - 1];
        String secondPart = SECOND_PARTS[RandomInt.getRandomInt(SECOND_PARTS.length) - 1];

        return prefix + secondPart + suffix;
    }
}
