package org.jarec.util;

public class PropertyHandler {
    private PropertyHandler() {}

    public static void load(String resourcePath) {
        GameConfig.load(resourcePath);
    }

    public static String get(String key) {
        return GameConfig.get(key, null);
    }

    public static String get(String key, String defaultValue) {
        return GameConfig.get(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return GameConfig.getInt(key, defaultValue);
    }

    public static long getLong(String key, long defaultValue) {
        return GameConfig.getLong(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return GameConfig.getBoolean(key, defaultValue);
    }
}
