package org.jarec.game;

import org.jarec.data.creature.Splurg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Combat {
    private static final Logger log = LoggerFactory.getLogger(Combat.class);

    // Method for attacking and determining the result of a combat round between two Splurgs
    public static void attack(Splurg splurgA, Splurg splurgB) {
        splurgA.setInCombat(true);
        splurgB.setInCombat(true);
        var damageA = fight(splurgA, splurgB);
        var damageB = fight(splurgB, splurgA);
        splurgA.reduceHealth(damageB - damageA);
        splurgB.reduceHealth(damageA - damageB);
    }

    private static int fight(Splurg splurg1, Splurg splurg2) {
        var strength = splurg1.getStrength().getValue();
        var toughness = splurg2.getToughness().getValue();
        var damage = strength - toughness;
        if (damage > 0) {
            return damage;
        }
        return 0;
    }
}
