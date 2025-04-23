package org.jarec.game;

import org.jarec.data.creature.Splurg;
import org.jarec.data.creature.attributes.Strength;
import org.jarec.data.creature.attributes.Toughness;
import org.junit.jupiter.api.Test;

import static org.jarec.game.Combat.attack;
import static org.mockito.Mockito.*;

class CombatTest {

    @Test
    void testStrongAttacker() {
        // Arrange
        Splurg attacker = mock(Splurg.class);
        Splurg defender = mock(Splurg.class);

        Strength attackerStrength = mock(Strength.class);
        when(attacker.getStrength()).thenReturn(attackerStrength);
        when(attackerStrength.getValue()).thenReturn(10);

        Toughness attackerToughness = mock(Toughness.class);
        when(attacker.getToughness()).thenReturn(attackerToughness);
        when(attackerToughness.getValue()).thenReturn(10);

        Strength defenderStrength = mock(Strength.class);
        when(defender.getStrength()).thenReturn(defenderStrength);
        when(defenderStrength.getValue()).thenReturn(10);

        Toughness defenderToughness = mock(Toughness.class);
        when(defender.getToughness()).thenReturn(defenderToughness);
        when(defenderToughness.getValue()).thenReturn(1);

        // Act
        Combat.attack(attacker, defender);

        verify(attacker).gainHealth(9);
        verify(defender).takeWound(9);
    }


    @Test
    void testStrongDefender() {
        // Arrange
        Splurg attacker = mock(Splurg.class);
        Splurg defender = mock(Splurg.class);

        Strength attackerStrength = mock(Strength.class);
        when(attacker.getStrength()).thenReturn(attackerStrength);
        when(attackerStrength.getValue()).thenReturn(10);

        Toughness attackerToughness = mock(Toughness.class);
        when(attacker.getToughness()).thenReturn(attackerToughness);
        when(attackerToughness.getValue()).thenReturn(1);

        Strength defenderStrength = mock(Strength.class);
        when(defender.getStrength()).thenReturn(defenderStrength);
        when(defenderStrength.getValue()).thenReturn(10);

        Toughness defenderToughness = mock(Toughness.class);
        when(defender.getToughness()).thenReturn(defenderToughness);
        when(defenderToughness.getValue()).thenReturn(10);

        // Act
        attack(attacker, defender);

        // Assert
        verify(attacker).takeWound(9);
        verify(defender).gainHealth(9);
    }
}
