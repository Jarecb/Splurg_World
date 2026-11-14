package org.jarec.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public final class GameConfig {
    private static final Set<String> POSITIVE_INTEGER_KEYS = Set.of(
            "gui.frame.width", "gui.frame.height", "gui.hive.default.number",
            "splurg.default.max.attribute", "splurg.default.max.toughness",
            "splurg.default.death.chance", "splurg.default.base.health",
            "splurg.default.size.multiplier", "splurg.default.energy.capacity",
            "splurg.default.breeding.delay", "splurg.default.stuck.break",
            "splurg.charisma.energy.divisor", "world.game.loop.sleeptime",
            "world.game.loop.pausedelaytime", "hive.default.spawn.rate",
            "hive.default.spawn.energy", "hive.default.size",
            "game.max.concurrent.low", "game.max.concurrent.medium",
            "game.max.concurrent.high", "game.max.concurrent.extreme",
            "zombie.default.activation.delay", "zombie.default.aggression",
            "zombie.default.health", "zombie.default.speed.multiplier",
            "world.game.slow.tick.warning.ms"
    );
    private static final Set<String> NON_NEGATIVE_INTEGER_KEYS = Set.of(
            "splurg.default.spawn.age", "splurg.default.pathing.randomness",
            "splurg.default.aggression.multiplier", "splurg.default.foraging.multiplier",
            "splurg.default.feeding.volume", "splurg.degradation",
            "gui.mouse.click.detection.range", "hive.default.position.inset",
            "hive.default.setup.energy", "hive.default.spawn.rate",
            "zombie.default.change", "zombie.default.strength.bonus"
    );
    private static final Set<String> BOOLEAN_KEYS = Set.of(
            "game.default.zombie", "game.default.herding"
    );
    private static final Set<String> STRING_KEYS = Set.of(
            "author", "version", "winning.image.red", "winning.image.blue",
            "winning.image.green", "winning.image.yellow", "winning.image.nobody",
            "winning.image.truce"
    );
    private static final Map<String, Integer> INTEGER_DEFAULTS = Map.ofEntries(
            Map.entry("gui.frame.width", 900), Map.entry("gui.frame.height", 700),
            Map.entry("gui.hive.default.number", 4), Map.entry("splurg.default.max.attribute", 10),
            Map.entry("splurg.default.max.toughness", 9), Map.entry("splurg.default.spawn.age", 5000),
            Map.entry("splurg.default.death.chance", 1000), Map.entry("splurg.default.base.health", 10),
            Map.entry("splurg.default.size.multiplier", 2), Map.entry("splurg.default.pathing.randomness", 5),
            Map.entry("splurg.default.aggression.multiplier", 5), Map.entry("splurg.default.foraging.multiplier", 15),
            Map.entry("splurg.default.feeding.volume", 1), Map.entry("splurg.default.energy.capacity", 5),
            Map.entry("splurg.default.breeding.delay", 100), Map.entry("splurg.default.stuck.break", 10),
            Map.entry("splurg.degradation", 1000), Map.entry("splurg.charisma.energy.divisor", 10),
            Map.entry("gui.mouse.click.detection.range", 20), Map.entry("hive.default.position.inset", 150),
            Map.entry("hive.default.setup.energy", 200), Map.entry("hive.default.spawn.rate", 5),
            Map.entry("hive.default.spawn.energy", 10), Map.entry("hive.default.size", 60),
            Map.entry("game.max.concurrent.low", 500), Map.entry("game.max.concurrent.medium", 750),
            Map.entry("game.max.concurrent.high", 1000), Map.entry("game.max.concurrent.extreme", 1500),
            Map.entry("zombie.default.activation.delay", 20), Map.entry("zombie.default.aggression", 10),
            Map.entry("zombie.default.change", 25), Map.entry("zombie.default.health", 25),
            Map.entry("zombie.default.speed.multiplier", 2), Map.entry("zombie.default.strength.bonus", 0),
            Map.entry("world.game.loop.sleeptime", 100), Map.entry("world.game.loop.pausedelaytime", 1000),
            Map.entry("world.game.slow.tick.warning.ms", 250)
    );
    private static final Properties EMPTY = new Properties();
    private static volatile Properties values = EMPTY;

    private GameConfig() {}

    public static synchronized void load(String resourcePath) {
        Properties loaded = new Properties();
        try (InputStream input = GameConfig.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new PropertyException("Property file not found: " + resourcePath);
            }
            loaded.load(input);
            load(loaded);
        } catch (IOException e) {
            throw new PropertyException("Failed to load properties from: " + resourcePath, e);
        }
    }

    public static synchronized void load(Properties properties) {
        Properties loaded = new Properties();
        loaded.putAll(properties);
        validate(loaded);
        values = loaded;
    }

    public static int getInt(String key, int fallback) {
        String raw = values.getProperty(key);
        int value;
        try {
            value = Integer.parseInt(raw == null ? Integer.toString(fallback) : raw);
        } catch (NumberFormatException e) {
            throw new PropertyException("Invalid integer property " + key + ": " + raw, e);
        }
        validateInteger(key, value);
        return value;
    }

    public static long getLong(String key, long fallback) {
        String raw = values.getProperty(key);
        try {
            long value = Long.parseLong(raw == null ? Long.toString(fallback) : raw);
            if (key.equals("world.random.seed") && value < -1) {
                throw new PropertyException("Property must be -1 or a non-negative seed: " + key);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new PropertyException("Invalid long property " + key + ": " + raw, e);
        }
    }

    public static boolean getBoolean(String key, boolean fallback) {
        String raw = values.getProperty(key);
        if (raw == null) return fallback;
        if (raw.equalsIgnoreCase("true")) return true;
        if (raw.equalsIgnoreCase("false")) return false;
        throw new PropertyException("Invalid boolean property " + key + ": " + raw);
    }

    public static String get(String key, String fallback) {
        return values.getProperty(key, fallback);
    }

    private static void validate(Properties properties) {
        for (String key : INTEGER_DEFAULTS.keySet()) {
            validateInteger(key, parseInteger(properties, key));
        }
        validateCrossPropertyRanges(properties);
        for (String key : BOOLEAN_KEYS) {
            String value = properties.getProperty(key);
            if (value != null && !value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                throw new PropertyException("Invalid boolean property " + key + ": " + value);
            }
        }
        for (String key : STRING_KEYS) {
            String value = properties.getProperty(key);
            if (value != null && value.isBlank()) {
                throw new PropertyException("Property must not be blank: " + key);
            }
        }
        String seed = properties.getProperty("world.random.seed", "-1");
        try {
            if (Long.parseLong(seed) < -1) {
                throw new PropertyException("Property must be -1 or a non-negative seed: world.random.seed");
            }
        } catch (NumberFormatException e) {
            throw new PropertyException("Invalid long property world.random.seed: " + seed, e);
        }
    }

    private static void validateCrossPropertyRanges(Properties properties) {
        int maxAttribute = parseInteger(properties, "splurg.default.max.attribute");
        int maxToughness = parseInteger(properties, "splurg.default.max.toughness");
        if (maxToughness > maxAttribute) {
            throw new PropertyException("splurg.default.max.toughness cannot exceed splurg.default.max.attribute");
        }

        int low = parseInteger(properties, "game.max.concurrent.low");
        int medium = parseInteger(properties, "game.max.concurrent.medium");
        int high = parseInteger(properties, "game.max.concurrent.high");
        int extreme = parseInteger(properties, "game.max.concurrent.extreme");
        if (low > medium || medium > high || high > extreme) {
            throw new PropertyException("Population limits must be ordered low <= medium <= high <= extreme");
        }
    }

    private static int parseInteger(Properties properties, String key) {
        String raw = properties.getProperty(key);
        try {
            return Integer.parseInt(raw == null ? INTEGER_DEFAULTS.get(key).toString() : raw);
        } catch (NumberFormatException e) {
            throw new PropertyException("Invalid integer property " + key + ": " + raw, e);
        }
    }

    private static void validateInteger(String key, int value) {
        if (key.equals("gui.hive.default.number") && (value < 2 || value > 4)) {
            throw new PropertyException("Property must be between 2 and 4: " + key);
        }
        if (POSITIVE_INTEGER_KEYS.contains(key) && value < 1) {
            throw new PropertyException("Property must be positive: " + key);
        }
        if (NON_NEGATIVE_INTEGER_KEYS.contains(key) && value < 0) {
            throw new PropertyException("Property must not be negative: " + key);
        }
        if (key.equals("splurg.default.max.attribute") && value > 100) {
            throw new PropertyException("Property must be at most 100: " + key);
        }
        if (key.equals("world.game.slow.tick.warning.ms") && value > 60_000) {
            throw new PropertyException("Property must be at most 60000: " + key);
        }
    }
}
