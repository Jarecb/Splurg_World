package org.jarec;

import org.jarec.gui.WorldFrame;
import org.jarec.util.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        new java.io.File("logs").mkdirs();

        try {
            log.info("Starting Splurg World");

            PropertyHandler.load("splurg.properties");
            log.info("Author: {}", PropertyHandler.get("author", "Property file not found!"));
            log.info("Version: {}", PropertyHandler.get("version", "??.??.??"));

            log.info("Creating Splurg World");
            WorldFrame.getInstance();
        } catch (Exception e) {
            log.error(e.toString());
        }
    }
}