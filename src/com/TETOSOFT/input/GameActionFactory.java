package com.TETOSOFT.input;

public class GameActionFactory {

    public static GameAction createGameAction(String name) {
        return createGameAction(name, GameAction.NORMAL);
    }

    public static GameAction createGameAction(String name, int behavior) {
        return new GameAction(name, behavior);
    }
}