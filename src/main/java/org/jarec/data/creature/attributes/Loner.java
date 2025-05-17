package org.jarec.data.creature.attributes;

import org.jarec.util.PropertyHandler;
import org.jarec.util.RandomInt;

public class Loner implements SettableAttribute {
    private int lonerValue = RandomInt.getRandomInt(Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10")));

    @Override
    public String getName() {
        return "Loner";
    }

    @Override
    public int getValue() {
        return lonerValue;
    }

    @Override
    public void setValue(int loner) {
        if (loner > Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10"))
                || loner <= 0) {
            throw new IllegalArgumentException("Loner outside of range: " + loner);
        }
        this.lonerValue = loner;
    }
}
