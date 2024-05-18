package com.TETOSOFT.input;

public class GameAction {
    private String name;
    private int behavior;
    private int amount;
    private GameState state;

    public static final int NORMAL = 0;
    public static final int DETECT_INITAL_PRESS_ONLY = 1;

    public GameAction(String name) {
        this(name, NORMAL);
    }

    public GameAction(String name, int behavior) {
        this.name = name;
        this.behavior = behavior;
        reset();
    }

    public String getName() {
        return name;
    }

    public void reset() {
        state = new ReleasedState();
        amount = 0;
    }

    public synchronized void tap() {
        press();
        release();
    }

    public synchronized void press() {
        press(1);
    }

    public synchronized void press(int amount) {
        state.press(amount);
    }

    public synchronized void release() {
        state.release();
    }

    public synchronized boolean isPressed() {
        return (getAmount() != 0);
    }

    public synchronized int getAmount() {
        int retVal = amount;
        if (retVal != 0) {
            if (state instanceof ReleasedState) {
                amount = 0;
            } else if (behavior == DETECT_INITAL_PRESS_ONLY && state instanceof PressedState) {
                state = new WaitForReleaseState();
                amount = 0;
            }
        }
        return retVal;
    }

    void move(int dx, int dy) {
    }
    
        private interface GameState {
        void press(int amount);
        void release();
    }

    private class ReleasedState implements GameState {
        @Override
        public void press(int amount) {
            GameAction.this.amount += amount;
            state = new PressedState();
        }

        @Override
        public void release() {
            // Do nothing since it's already in the released state
        }
    }

    private class PressedState implements GameState {
        @Override
        public void press(int amount) {
            GameAction.this.amount += amount;
        }

        @Override
        public void release() {
            state = new ReleasedState();
        }
    }

    private class WaitForReleaseState implements GameState {
        @Override
        public void press(int amount) {
            // Do nothing since it's waiting for release
        }

        @Override
        public void release() {
            GameAction.this.amount = 0;
            state = new ReleasedState();
        }
    }
}