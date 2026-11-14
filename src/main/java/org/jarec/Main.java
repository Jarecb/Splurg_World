package org.jarec;

import org.jarec.gui.WorldFrame;
import org.jarec.gui.UiTheme;
import org.jarec.util.PropertyHandler;
import org.jarec.util.RandomInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            Files.createDirectories(Path.of("logs"));
            log.info("Starting Splurg World");

            PropertyHandler.load("splurg.properties");
            long configuredSeed = PropertyHandler.getLong("world.random.seed", -1);
            long seed = configuredSeed == -1 ? new Random().nextLong() & Long.MAX_VALUE : configuredSeed;
            RandomInt.setSeed(seed);
            log.info("Simulation seed: {}", seed);
            log.info("Author: {}", PropertyHandler.get("author", "Property file not found!"));
            log.info("Version: {}", PropertyHandler.get("version", "??.??.??"));

            log.info("Creating Splurg World");
            SwingUtilities.invokeLater(() -> {
                try {
                    UiTheme.install();
                    WorldFrame frame = WorldFrame.getInstance();
                    if (!frame.isVisible()) {
                        frame.setVisible(true);
                    }
                } catch (Exception e) {
                    log.error("Failed to initialize the Splurg World window", e);
                }
            });
        } catch (IOException e) {
            log.error("Failed to create the logs directory", e);
        } catch (Exception e) {
            log.error("Failed to start Splurg World", e);
        }
    }
}