package org.jarec.game;

import org.jarec.data.Location;
import org.jarec.data.Nest;
import org.jarec.game.resources.Nests;
import org.jarec.game.resources.Splurgs;
import org.jarec.util.PropertyHandler;

import java.awt.*;

public class GameStart {

    public GameStart(){
        Nests.getInstance().clearNests();

        Nest nestOne = new Nest(new Location(100, 100), Color.RED, "The Hive");
        nestOne.addFood(Integer.parseInt(PropertyHandler.get("nest.default.setup.food", "100")));
        Nests.getInstance().addNest(nestOne);

        Nest nestTwo = new Nest(new Location(400, 400), Color.BLUE, "The Dark Hive");
        nestTwo.addFood(Integer.parseInt(PropertyHandler.get("nest.default.setup.food", "100")));
        Nests.getInstance().addNest(nestTwo);

        Splurgs.getInstance().clearSplurgs();

        GameLoop.getInstance().start();
    }


}
