package org.jarec.game;

public enum GameEndState {
    BLUE("Blue_Wins.png"),
    GREEN("Green_Wins.png"),
    RED("Red_Wins.png"),
    YELLOW("Yellow_Wins.png"),
    ZOMBIE("Nobody_Wins.png"),
    STALEMATE("Truce.png");

    private final String fileName;

    GameEndState(String fileName) {
        this.fileName = fileName;
    }

    public String getfileName() {
        return fileName;
    }
}
