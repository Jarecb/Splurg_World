package org.jarec.game;

import org.jarec.data.creature.Splurg;
import org.jarec.data.creature.attributes.Strength;
import org.jarec.data.creature.attributes.Toughness;
import org.junit.jupiter.api.Test;

import static org.jarec.game.Combat.attack;
import static org.mockito.Mockito.*;

class CombatTest {

    @Test
    public void testAttack() {
        // Arrange
        Splurg attacker = mock(Splurg.class);
        Splurg defender = mock(Splurg.class);

        // Mocking the Attribute class for Strength and Toughness
        Strength strength = mock(Strength.class);
        when(attacker.getStrength()).thenReturn(strength);
        when(strength.getValue()).thenReturn(10); // Attacker's strength is 10

        Toughness toughness = mock(Toughness.class);
        when(defender.getToughness()).thenReturn(toughness);
        when(toughness.getValue()).thenReturn(4); // Defender's toughness is 4

        // Act
        attack(attacker, defender);

        // Assert
        verify(attacker).reduceHealth(-6);  // Attacker should gain 6 health (0 - (10 - 4))
        verify(defender).reduceHealth(6);   // Defender should lose 6 health
    }
}
