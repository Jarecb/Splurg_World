package org.jarec.game;

import org.jarec.data.creature.Splurg;
import org.jarec.data.creature.Zombie;

public class Combat {

    private Combat() {
    }

    // Method for attacking and determining the result of a combat round between two Splurgs
    public static void attack(Splurg splurgA, Splurg splurgB) {
        var aWoundsB = wound(splurgA, splurgB);
        var bWoundsA = wound(splurgB, splurgA);
        var aIsAZombie = splurgA instanceof Zombie;
        var bIsAZombie = splurgB instanceof Zombie;

        if (aWoundsB > 0) {
            splurgB.takeWound(aWoundsB);
            if(aIsAZombie) {
                splurgB.setInfectedByZombie(true);
            }
            if (!aIsAZombie && !bIsAZombie){
                splurgA.gainHealth(aWoundsB);
            }
        }

        if (bWoundsA > 0) {
            splurgA.takeWound(bWoundsA);
            if(bIsAZombie){
                splurgA.setInfectedByZombie(true);
            }
            if (!aIsAZombie && !bIsAZombie){
                splurgB.gainHealth(bWoundsA);
            }
        }

        GameLoop.getInstance().incrementCombatsPerTurn();
    }

    private static int wound(Splurg splurg1, Splurg splurg2) {
        var strength = splurg1.getStrength().getValue();
        var toughness = splurg2.getToughness().getValue();
        var damage = strength - toughness;
        return Math.max(0, damage);
    }
}
