package org.jarec.data.creature.attributes;

import org.jarec.util.PropertyHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForagingTest {

    @Test
    void getNameTest() {
        SettableAttribute foraging = new Foraging();
        assertEquals("Foraging", foraging.getName());
    }

    @Test
    void getValueTest_isDefaultValueInRange() {
        int max = Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10"));

        int loop = 100;
        // Soak test as number is random
        while(loop > 0)
        {
            loop--;
            SettableAttribute foraging = new Foraging();
            assertTrue(foraging.getValue() <= max && foraging.getValue() > 0, "Foraging = " + foraging.getValue());
        }
    }

    @Test
    void setValueTest_throwsExceptionIfNotInRange() {
        int max = Integer.parseInt(PropertyHandler.get("splurg.default.max.attribute", "10"));
        SettableAttribute foraging = new Foraging();
        assertThrows(IllegalArgumentException.class, () -> foraging.setValue(0));
        assertThrows(IllegalArgumentException.class, () -> foraging.setValue(max + 1));
    }
}