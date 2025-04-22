package org.jarec.game;

import org.jarec.data.Hive;
import org.jarec.data.Location;
import org.jarec.game.resources.Hives;
import org.jarec.game.resources.Splurgs;
import org.jarec.gui.WorldFrame;
import org.jarec.util.PropertyHandler;

import java.awt.*;

public class GameStart {

    public GameStart(int hiveCount, int hiveEnergy, boolean zombiesActive) {
        Hives.getInstance().clearHives();

        var worldWidth = WorldFrame.getInstance().getWorldPanel().getWorldWidth();
        var worldHeight = WorldFrame.getInstance().getWorldPanel().getWorldHeight();
        var hiveInset = Integer.parseInt(PropertyHandler.get("hive.default.position.inset", "150"));

        var locationOne = new Location(0, 0);
        var locationTwo = new Location(0, 0);
        var locationThree = new Location(0, 0);
        var locationFour = new Location(0, 0);

        if (hiveCount == 2) {
            locationOne = new Location(hiveInset, worldHeight / 2);
            locationTwo = new Location(worldWidth - hiveInset, worldHeight / 2);
        } else if (hiveCount == 3) {
            locationOne = new Location(hiveInset, worldHeight - hiveInset);
            locationTwo = new Location(worldWidth - hiveInset, worldHeight - hiveInset);
            locationThree = new Location(worldWidth / 2, hiveInset);
        } else if (hiveCount == 4) {
            locationOne = new Location(hiveInset, worldHeight - hiveInset);
            locationTwo = new Location(worldWidth - hiveInset, worldHeight - hiveInset);
            locationThree = new Location(hiveInset, hiveInset);
            locationFour = new Location(worldWidth - hiveInset, hiveInset);
        }

        Hive hiveOne = new Hive(locationOne, new Color(255, 0, 0, 128), "Red Hive", false);
        hiveOne.addEnergy(hiveEnergy);

        Hive hiveTwo = new Hive(locationTwo, new Color(0, 0, 255, 128),
                "Blue Hive", false);
        hiveTwo.addEnergy(hiveEnergy);

        Hive hiveThree = new Hive(locationThree, new Color(255, 255, 0, 128), "Yellow Hive", false);
        hiveThree.addEnergy(hiveEnergy);

        Hive hiveFour = new Hive(locationFour, new Color(0, 255, 0, 128),
                "Green Hive", false);
        hiveFour.addEnergy(hiveEnergy);

        Hives.getInstance().addHive(hiveOne);
        Hives.getInstance().addHive(hiveTwo);
        if (hiveCount >= 3) {
            Hives.getInstance().addHive(hiveThree);
        }
        if (hiveCount == 4) {
            Hives.getInstance().addHive(hiveFour);
        }

        if (zombiesActive) {
//            Hive zombieHive = new Hive(new Location(0, 0), new Color(64, 64, 64, 128),
//                    "Zombie Hive", true);
            Hive zombieHive = new Hive(new Location(0, 0), new Color(0, 0, 0, 255),
                    "Zombie Hive", true);
            Hives.getInstance().addHive(zombieHive);
        }

        Splurgs.getInstance().clearSplurgs();

        GameLoop.getInstance().start(zombiesActive);
    }
}