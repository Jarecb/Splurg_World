package org.jarec.data.creature.attributes;

import org.jarec.util.PropertyHandler;

public class Charisma implements Attribute {

    private int charismaValue;

    @Override
    public String getName() {
        return "Charisma";
    }

    public void setValue(Size size, int energy) {
        charismaValue = (int) ((size.getValue() / 2) + (energy / Integer.parseInt(PropertyHandler.get("splurg.charisma.energy.divisor", "20"))));
        if (charismaValue > 10) {
            charismaValue = 10;
        }
    }

    @Override
    public int getValue() {
        return charismaValue;
    }
}
