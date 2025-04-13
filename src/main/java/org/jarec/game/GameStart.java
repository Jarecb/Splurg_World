package org.jarec.game;

import org.jarec.data.Hive;
import org.jarec.data.Location;
import org.jarec.game.resources.Hives;
import org.jarec.game.resources.Splurgs;
import org.jarec.gui.WorldFrame;
import org.jarec.util.PropertyHandler;

import java.awt.*;

public class GameStart {

    public GameStart(int hiveCount, int hiveEnergy){
        Hives.getInstance().clearHives();

        var worldWidth = WorldFrame.getInstance().getWorldPanel().getWorldWidth();
        var worldHeight = WorldFrame.getInstance().getWorldPanel().getWorldHeight();
        var hiveInset = Integer.parseInt(PropertyHandler.get("hive.default.position.inset", "150"));

        Hive hiveOne = new Hive(new Location(hiveInset, hiveInset), new Color(255, 0, 0, 128), "Red Hive");
        hiveOne.addEnergy(hiveEnergy);
        Hives.getInstance().addHive(hiveOne);

        Hive hiveTwo = new Hive(new Location(worldWidth - hiveInset, worldHeight - hiveInset), new Color(0, 0, 255, 128),
                "Blue Hive");
        hiveTwo.addEnergy(hiveEnergy);
        Hives.getInstance().addHive(hiveTwo);

        if (hiveCount == 4) {
            Hive hiveThree = new Hive(new Location(worldWidth - hiveInset, hiveInset), new Color(255, 255, 0, 128), "Yellow Hive");
            hiveThree.addEnergy(hiveEnergy);
            Hives.getInstance().addHive(hiveThree);

            Hive hiveFour = new Hive(new Location(hiveInset, worldHeight - hiveInset), new Color(0, 255, 0, 128),
                    "Green Hive");
            hiveFour.addEnergy(hiveEnergy);
            Hives.getInstance().addHive(hiveFour);
        }

        Splurgs.getInstance().clearSplurgs();

        GameLoop.getInstance().start();
    }


}
