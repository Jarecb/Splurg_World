package org.jarec.game;

import org.jarec.data.Hive;

public class GameEndCheck {

    private GameEndCheck(){}

    public static GameEndState determineEndState(Hive winningHive) {
        if (winningHive == null) {
            return GameEndState.STALEMATE;
        }

        if (winningHive.isZombie()) {
            return GameEndState.ZOMBIE;
        }

        return switch (winningHive.getName().toLowerCase()) {
            case String s when s.contains("green") -> GameEndState.GREEN;
            case String s when s.contains("red") -> GameEndState.RED;
            case String s when s.contains("blue") -> GameEndState.BLUE;
            case String s when s.contains("yellow") -> GameEndState.YELLOW;
            default -> GameEndState.STALEMATE;
        };
    }
}
