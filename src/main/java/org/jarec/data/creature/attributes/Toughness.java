package org.jarec.data.creature.attributes;

import org.jarec.util.PropertyHandler;
import org.jarec.util.RandomInt;

public class Toughness implements SettableAttribute {
    private int toughness = RandomInt.getRandomInt(Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10")));

    @Override
    public String getName() {
        return "Toughness";
    }

    @Override
    public int getValue() {
        return this.toughness;
    }

    @Override
    public void setValue(int toughness) {
        if (toughness > Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10"))
            || toughness <= 0)
        {
            throw new IllegalArgumentException("Toughness outside of range: " + toughness);
        }
        this.toughness = toughness;
    }
}
