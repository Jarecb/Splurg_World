package org.jarec.data.creature.attributes;

import org.jarec.util.PropertyHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StrengthTest {

    @Test
    void getNameTest() {
        SettableAttribute strength = new Strength();
        assertEquals("Strength", strength.getName());
    }

    @Test
    void getValueTest_isDefaultValueInRange() {
        int max = Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10"));

        int loop = 100;
        // Soak test as number is random
        while(loop > 0)
        {
            loop--;
            SettableAttribute strength = new Strength();
            assertTrue(strength.getValue() <= max && strength.getValue() > 0, "Strength = " + strength.getValue());
        }
    }
}