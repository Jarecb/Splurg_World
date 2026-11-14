package org.jarec.util;

import java.util.Random;
import java.util.Collections;
import java.util.List;

public class RandomInt {
    private RandomInt() {
    }

    private static Random random = new Random();

    public static synchronized int getRandomInt(int max) {
        if (max < 1) {
            throw new IllegalArgumentException("Input must be >= 1");
        }
        return random.nextInt(max) + 1;
    }

    public static synchronized boolean getRandomBoolean() {
        return random.nextBoolean();
    }

    public static synchronized void shuffle(List<?> values) {
        Collections.shuffle(values, random);
    }

    public static synchronized void setSeed(long seed) {
        random = new Random(seed);
    }
}
