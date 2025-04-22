package org.jarec.data.creature.attributes;

import org.jarec.util.PropertyHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToughnessTest {

    @Test
    void getNameTest() {
        SettableAttribute toughness = new Toughness();
        assertEquals("Toughness", toughness.getName());
    }

    @Test
    void getValueTest_isDefaultValueInRange() {
        int max = Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10"));

        int loop = 100;
        // Soak test as number is random
        while(loop > 0)
        {
            loop--;
            SettableAttribute toughness = new Toughness();
            assertTrue(toughness.getValue() <= max && toughness.getValue() > 0, "Toughness = " + toughness.getValue());
        }
    }
}