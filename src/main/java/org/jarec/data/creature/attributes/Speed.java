package org.jarec.data.creature.attributes;

import org.jarec.util.PropertyHandler;

public class Speed implements Attribute {
    public Speed(Size size) {
        this.speed = Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10")) - size.getValue();
    }

    private int speed = 0;

    @Override
    public String getName() {
        return "Speed";
    }

    @Override
    public int getValue() {
        return this.speed;
    }
}
