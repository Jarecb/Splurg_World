package org.jarec.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameConfigTest {
    @AfterEach
    void restoreDefaults() {
        GameConfig.load("splurg.properties");
    }

    @Test
    void readsTypedValuesFromValidatedConfiguration() {
        Properties properties = new Properties();
        properties.setProperty("gui.frame.width", "1024");
        properties.setProperty("game.default.herding", "true");
        GameConfig.load(properties);

        assertEquals(1024, PropertyHandler.getInt("gui.frame.width", 800));
        org.junit.jupiter.api.Assertions.assertTrue(PropertyHandler.getBoolean("game.default.herding", false));
    }

    @Test
    void rejectsInvalidValuesAtConfigurationLoad() {
        Properties properties = new Properties();
        properties.setProperty("splurg.default.max.attribute", "0");

        assertThrows(PropertyException.class, () -> GameConfig.load(properties));

        properties.setProperty("splurg.default.max.attribute", "10");
        properties.setProperty("gui.hive.default.number", "5");
        assertThrows(PropertyException.class, () -> GameConfig.load(properties));

        properties.setProperty("gui.hive.default.number", "4");
        properties.setProperty("game.max.concurrent.low", "900");
        assertThrows(PropertyException.class, () -> GameConfig.load(properties));
    }

    @Test
    void rejectsMalformedTypedReads() {
        Properties properties = new Properties();
        properties.setProperty("gui.frame.width", "wide");

        assertThrows(PropertyException.class, () -> GameConfig.load(properties));
    }
}
