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

        Nest nestOne = new Nest(new Location(nestInset, nestInset), Color.RED, "Red Hive");
        nestOne.addFood(nestFood);
        Nests.getInstance().addNest(nestOne);

        Nest nestTwo = new Nest(new Location(worldWidth - nestInset, worldHeight - nestInset), Color.BLUE,
                "Blue Hive");
        nestTwo.addFood(nestFood);
        Nests.getInstance().addNest(nestTwo);

        if (nestCount == 4) {
            Nest nestThree = new Nest(new Location(worldWidth - nestInset, nestInset), Color.YELLOW, "Yellow Hive");
            nestThree.addFood(nestFood);
            Nests.getInstance().addNest(nestThree);

            Nest nestFour = new Nest(new Location(nestInset, worldHeight - nestInset), Color.GREEN,
                    "Green Hive");
            nestFour.addFood(nestFood);
            Nests.getInstance().addNest(nestFour);
        }

        Splurgs.getInstance().clearSplurgs();

        GameLoop.getInstance().start();
    }


}
