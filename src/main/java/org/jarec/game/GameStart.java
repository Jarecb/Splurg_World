package org.jarec.game;

import org.jarec.data.Location;
import org.jarec.data.Nest;
import org.jarec.game.resources.Nests;
import org.jarec.game.resources.Splurgs;
import org.jarec.gui.WorldFrame;
import org.jarec.util.PropertyHandler;

import java.awt.*;

public class GameStart {

    public GameStart(int nestCount, int nestFood){
        Nests.getInstance().clearNests();

        var worldWidth = WorldFrame.getInstance().getWorldPanel().getWorldWidth();
        var worldHeight = WorldFrame.getInstance().getWorldPanel().getWorldHeight();
        var nestInset = Integer.parseInt(PropertyHandler.get("nest.default.position.inset", "150"));

        Nest nestOne = new Nest(new Location(nestInset, nestInset), new Color(255, 0, 0, 128), "Red Hive");
        nestOne.addFood(nestFood);
        Nests.getInstance().addNest(nestOne);

        Nest nestTwo = new Nest(new Location(worldWidth - nestInset, worldHeight - nestInset), new Color(0, 0, 255, 128),
                "Blue Hive");
        nestTwo.addFood(nestFood);
        Nests.getInstance().addNest(nestTwo);

        if (nestCount == 4) {
            Nest nestThree = new Nest(new Location(worldWidth - nestInset, nestInset), new Color(255, 255, 0, 128), "Yellow Hive");
            nestThree.addFood(nestFood);
            Nests.getInstance().addNest(nestThree);

            Nest nestFour = new Nest(new Location(nestInset, worldHeight - nestInset), new Color(0, 255, 0, 128),
                    "Green Hive");
            nestFour.addFood(nestFood);
            Nests.getInstance().addNest(nestFour);
        }

        Splurgs.getInstance().clearSplurgs();

        GameLoop.getInstance().start();
    }


}
