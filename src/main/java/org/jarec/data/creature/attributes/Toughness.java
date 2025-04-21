package org.jarec.data.creature.attributes;

import org.jarec.util.PropertyHandler;
import org.jarec.util.RandomInt;

public class Toughness implements SettableAttribute {
    private int toughnessValue
            = RandomInt.getRandomInt(Integer.parseInt(PropertyHandler.get("splurg.default.max.toughness", "10")));
    private static final int MAX_TOUGHNESS = Integer.parseInt(PropertyHandler.get("splurg.default.max.toughness", "10"));

    @Override
    public String getName() {
        return "Toughness";
    }

    @Override
    public int getValue() {
        return toughnessValue;
    }

    @Override
    public void setValue(int toughness) {

        if (toughness > MAX_TOUGHNESS
                || toughness <= 0) {
            toughness = MAX_TOUGHNESS;
        }
        toughnessValue = toughness;
    }
}
