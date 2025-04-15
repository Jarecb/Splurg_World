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

        Hive hiveOne = new Hive(new Location(hiveInset, hiveInset), new Color(255, 0, 0, 128), "Red Hive", false);
        hiveOne.addEnergy(hiveEnergy);
        Hives.getInstance().addHive(hiveOne);

        Hive hiveTwo = new Hive(new Location(worldWidth - hiveInset, worldHeight - hiveInset), new Color(0, 0, 255, 128),
                "Blue Hive", false);
        hiveTwo.addEnergy(hiveEnergy);
        Hives.getInstance().addHive(hiveTwo);

        if (hiveCount == 4) {
            Hive hiveThree = new Hive(new Location(worldWidth - hiveInset, hiveInset), new Color(255, 255, 0, 128), "Yellow Hive", false);
            hiveThree.addEnergy(hiveEnergy);
            Hives.getInstance().addHive(hiveThree);

            Hive hiveFour = new Hive(new Location(hiveInset, worldHeight - hiveInset), new Color(0, 255, 0, 128),
                    "Green Hive", false);
            hiveFour.addEnergy(hiveEnergy);
            Hives.getInstance().addHive(hiveFour);
        }

        if (zombiesActive) {
            Hive zombieHive = new Hive(new Location(0, 0), new Color(0, 0, 0, 255),
                    "Zombie Hive", true);
            Hives.getInstance().addHive(zombieHive);
        }

        Splurgs.getInstance().clearSplurgs();

        GameLoop.getInstance().start(zombiesActive);
    }
}