package org.jarec;

import org.jarec.game.GameLoop;
import org.jarec.gui.WorldFrame;
import org.jarec.util.PropertyHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Starting Splurg World");

        PropertyHandler.load("splurg.properties");
        log.info("Author: {}", PropertyHandler.get("author", "Property file not found!"));
        log.info("Version: {}", PropertyHandler.get("version", "??.??.??"));

        log.info("Creating Splurg World");
        WorldFrame frame = WorldFrame.getInstance();
    }
}