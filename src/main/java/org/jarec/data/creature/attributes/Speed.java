package org.jarec.data.creature.attributes;

import org.jarec.util.PropertyHandler;

public class Speed implements Attribute {
    public Speed(Size size) {
        speedValue = PropertyHandler.getInt("splurg.default.max.attribute", 10) - size.getValue();
    }

    public Speed(Size size, int multiplier) {
        speedValue = multiplier * PropertyHandler.getInt("splurg.default.max.attribute", 10) - size.getValue();
    }

    private final int speedValue;

    @Override
    public String getName() {
        return "Speed";
    }

    @Override
    public int getValue() {
        return speedValue;
    }
}
