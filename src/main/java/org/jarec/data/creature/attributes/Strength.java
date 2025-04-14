package org.jarec.data.creature.attributes;

import org.jarec.util.PropertyHandler;
import org.jarec.util.RandomInt;

public class Strength implements SettableAttribute {
    private int strengthValue = RandomInt.getRandomInt(Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10")));

    @Override
    public String getName() {
        return "Strength";
    }

    @Override
    public int getValue() {
        return this.strengthValue;
    }

    @Override
    public void setValue(int strength) {
        if (strength > Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10"))
                || strength <= 0) {
            throw new IllegalArgumentException("Strength outside of range: " + strength);
        }
        this.strengthValue = strength;
    }
}
