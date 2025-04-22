package org.jarec.game;

import org.jarec.util.PropertyHandler;

public enum GameEndState {
    BLUE(PropertyHandler.get("winning.image.blue", "Blue_Wins.png")),
    GREEN(PropertyHandler.get("winning.image.green", "Green_Wins.png")),
    RED(PropertyHandler.get("winning.image.red", "Red_Wins.png")),
    YELLOW(PropertyHandler.get("winning.image.yellow", "Yellow_Wins.png")),
    ZOMBIE(PropertyHandler.get("winning.image.nobody", "Nobody_Wins.png")),
    STALEMATE(PropertyHandler.get("winning.image.truce", "Truce.png"));

    private final String fileName;

    GameEndState(String fileName) {
        this.fileName = fileName;
    }

    public String getfileName() {
        return fileName;
    }
}
