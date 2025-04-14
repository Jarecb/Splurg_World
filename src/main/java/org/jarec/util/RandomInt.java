package org.jarec.util;

import java.util.Random;

public class RandomInt {
    private RandomInt() {
    }

    private static final Random random = new Random();

    public static int getRandomInt(int max) {
        if (max < 1) {
            throw new IllegalArgumentException("Input must be >= 1");
        }
        return random.nextInt(max) + 1;
    }
}
