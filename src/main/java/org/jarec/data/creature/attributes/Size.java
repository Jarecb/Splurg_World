package org.jarec.data.creature.attributes;

public class Size implements Attribute {
    public Size(Toughness toughness, Strength strength) {
        sizeValue = (toughness.getValue() + strength.getValue()) / 2;
    }

    private final int sizeValue;

    @Override
    public String getName() {
        return "Size";
    }

    @Override
    public int getValue() {
        return sizeValue;
    }
}
