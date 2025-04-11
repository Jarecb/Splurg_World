package org.jarec.data.creature.attributes;

import org.jarec.data.Location;
import org.jarec.data.Nest;
import org.jarec.data.creature.Splurg;
import org.jarec.game.resources.Splurgs;
import org.jarec.util.PropertyHandler;
import org.jarec.util.RandomInt;

import java.util.List;

public class Foraging implements SettableAttribute {
    private int foraging = RandomInt.getRandomInt(Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10")));

    @Override
    public String getName() {
        return "Foraging";
    }

    @Override
    public int getValue() {
        return this.foraging;
    }

    @Override
    public void setValue(int foraging) {
        if (foraging > Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10"))
            || foraging <= 0)
        {
            throw new IllegalArgumentException("Foraging outside of range: " + foraging);
        }
        this.foraging = foraging;
    }
}
