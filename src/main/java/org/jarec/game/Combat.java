package org.jarec.game;

import org.jarec.Main;
import org.jarec.game.resources.Splurgs;
import org.jarec.data.creature.Splurg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Combat {
    private static final Logger log = LoggerFactory.getLogger(Combat.class);

    // Method for attacking and determining the result of a combat round between two Splurgs
    public static void attack(Splurg splurg1, Splurg splurg2) {
        var strength = splurg1.getStrength().getValue();
        var toughness = splurg2.getToughness().getValue();
        var damage = strength - toughness;
//        log.info("A Splurg with Strength {} attacked a Splurg with Toughness {} causing {} damage", strength, toughness, damage);
        splurg1.reduceHealth(0 - damage);
        splurg2.reduceHealth(damage);
    }
}
