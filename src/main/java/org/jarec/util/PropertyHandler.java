package org.jarec.util;

import java.io.*;
import java.util.Properties;

public class PropertyHandler {
    private static final Properties properties = new Properties();
    private static boolean isLoaded = false;

    private PropertyHandler() {}

    public static void load(String resourcePath) {
        if (!isLoaded) {
            try (InputStream input = PropertyHandler.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (input == null) {
                    throw new RuntimeException("Property file not found: " + resourcePath);
                }
                properties.load(input);
                isLoaded = true;
            } catch (IOException e) {
                throw new RuntimeException("Failed to load properties from: " + resourcePath, e);
            }
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
