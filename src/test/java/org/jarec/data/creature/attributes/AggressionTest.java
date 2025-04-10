package org.jarec.data.creature.attributes;

import org.jarec.util.PropertyHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AggressionTest {

    @Test
    void getNameTest() {
        SettableAttribute aggression = new Aggression();
        assertEquals("Aggression", aggression.getName());
    }

    @Test
    void getValueTest_isDefaultValueInRange() {
        int max = Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10"));

        int loop = 100;
        // Soak test as number is random
        while(loop > 0)
        {
            loop--;
            SettableAttribute aggression = new Aggression();
            assertTrue(aggression.getValue() <= max && aggression.getValue() > 0, "Aggression = " + aggression.getValue());
        }
    }

    @Test
    void setValueTest_throwsExceptionIfNotInRange() {
        int max = Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10"));
        SettableAttribute aggression = new Aggression();
        assertThrows(IllegalArgumentException.class, () -> aggression.setValue(0));
        assertThrows(IllegalArgumentException.class, () -> aggression.setValue(max + 1));
    }
}