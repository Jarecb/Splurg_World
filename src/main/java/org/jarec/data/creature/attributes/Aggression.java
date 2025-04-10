package org.jarec.data.creature.attributes;

import org.jarec.util.PropertyHandler;
import org.jarec.util.RandomInt;

public class Aggression implements SettableAttribute {
    private int aggression = RandomInt.getRandomInt(Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10")));

    @Override
    public String getName() {
        return "Aggression";
    }

    @Override
    public int getValue() {
        return this.aggression;
    }

    @Override
    public void setValue(int aggression) {
        if (aggression > Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10"))
            || aggression <= 0)
        {
            throw new IllegalArgumentException("Aggression outside of range: " + aggression);
        }
        this.aggression = aggression;
    }
}
