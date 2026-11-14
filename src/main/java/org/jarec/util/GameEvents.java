package org.jarec.util;

import java.util.Objects;
import java.util.function.Consumer;

public final class GameEvents {
    private static volatile Consumer<String> statusListener = message -> {};

    private GameEvents() {}

    public static void setStatusListener(Consumer<String> listener) {
        statusListener = Objects.requireNonNull(listener);
    }

    public static void publishStatus(String message) {
        statusListener.accept(message);
    }
}
