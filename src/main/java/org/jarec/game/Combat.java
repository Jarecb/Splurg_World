package org.jarec.game;

import org.jarec.data.creature.Splurg;
import org.jarec.game.resources.Splurgs;

public class Combat {

    private Combat(){}

    // Method for attacking and determining the result of a combat round between two Splurgs
    public static void attack(Splurg splurgA, Splurg splurgB) {
        var damageA = fight(splurgA, splurgB);
        var damageB = fight(splurgB, splurgA);
        splurgA.reduceHealth(damageB - damageA);
        splurgB.reduceHealth(damageA - damageB);
        GameLoop.getInstance().incrementCombatsPerTurn();
    }

    private static int fight(Splurg splurg1, Splurg splurg2) {
        var strength = splurg1.getStrength().getValue();
        var toughness = splurg2.getToughness().getValue();
        var damage = strength - toughness;
        return Math.max(0, damage);
    }
}
