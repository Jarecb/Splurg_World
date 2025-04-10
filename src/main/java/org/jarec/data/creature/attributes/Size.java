package org.jarec.data.creature.attributes;

import org.jarec.util.PropertyHandler;
import org.jarec.util.RandomInt;

public class Size implements Attribute {
    public Size(Toughness toughness, Strength strength) {
        this.size = (toughness.getValue() + strength.getValue()) / 2;
    }

    private int size = 0;

    @Override
    public String getName() {
        return "Size";
    }

    @Override
    public int getValue() {
        return this.size;
    }
}
