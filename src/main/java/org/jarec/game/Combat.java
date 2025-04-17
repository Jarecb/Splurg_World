package org.jarec.game;

import org.jarec.data.creature.Splurg;
import org.jarec.data.creature.Zombie;

public class Combat {

    private Combat() {
    }

    // Method for attacking and determining the result of a combat round between two Splurgs
    public static void attack(Splurg splurgA, Splurg splurgB) {
        var damageA = wound(splurgA, splurgB);
        var damageB = wound(splurgB, splurgA);
        // Positive results are damage done, negative are health gain
        var baResult = damageB - damageA;
        var abResult = damageA - damageB;
        splurgA.reduceHealth(baResult);
        splurgB.reduceHealth(abResult);
        GameLoop.getInstance().incrementCombatsPerTurn();
    }

    private static int wound(Splurg splurg1, Splurg splurg2) {
        var strength = splurg1.getStrength().getValue();
        var toughness = splurg2.getToughness().getValue();
        var damage = strength - toughness;
        return Math.max(0, damage);
    }
}
