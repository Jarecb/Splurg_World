package org.jarec.data.creature.attributes;

import org.jarec.util.PropertyHandler;
import org.jarec.util.RandomInt;

public class Strength implements SettableAttribute {
    private int strengthValue = RandomInt.getRandomInt(Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10")));
    private static final int MAX_STRENGTH = Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10"));

    @Override
    public String getName() {
        return "Strength";
    }

    @Override
    public int getValue() {
        return strengthValue;
    }

    @Override
    public void setValue(int strength) {
        if (strength > MAX_STRENGTH
                || strength <= 0) {
            strengthValue = MAX_STRENGTH;
        }
        strengthValue = strength;
    }
}
