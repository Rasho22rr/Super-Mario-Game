
package com.TETOSOFT.input;


class KeyCommand implements Command {
    private GameAction action;

    public KeyCommand(GameAction action) {
        this.action = action;
    }

    @Override
    public void execute() {
        action.press();
    }

}

class MouseClickCommand implements Command {
    private GameAction action;

    public MouseClickCommand(GameAction action) {
        this.action = action;
    }

    @Override
    public void execute() {
        action.press();
    }

}

class MouseMoveCommand implements Command {
    private GameAction action;
    private int dx, dy;

    public MouseMoveCommand(GameAction action, int dx, int dy) {
        this.action = action;
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void execute() {
        action.move(dx, dy); // Assuming the GameAction has a method to handle mouse movement
    }

}