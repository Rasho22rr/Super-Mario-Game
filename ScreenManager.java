package com.TETOSOFT.graphics;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;

// Singleton 
public class ScreenManager {
    private static ScreenManager instance;
    private GraphicsDevice device;

    private ScreenManager() {
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        device = environment.getDefaultScreenDevice();
    }

    public static ScreenManager getInstance() {
        if (instance == null) {
            instance = new ScreenManager();
        }
        return instance;
    }

    public DisplayMode[] getCompatibleDisplayModes() {
        return device.getDisplayModes();
    }

    public DisplayMode findFirstCompatibleMode(DisplayMode[] modes) {
        DisplayMode goodModes[] = device.getDisplayModes();
        for (DisplayMode mode : modes) {
            for (DisplayMode goodMode : goodModes) {
                if (displayModesMatch(mode, goodMode)) {
                    return mode;
                }
            }
        }
        return null;
    }

    public DisplayMode getCurrentDisplayMode() {
        return device.getDisplayMode();
    }

    public boolean displayModesMatch(DisplayMode mode1, DisplayMode mode2) {
        if (mode1.getWidth() != mode2.getWidth() || mode1.getHeight() != mode2.getHeight()) {
            return false;
        }
        if (mode1.getBitDepth() != DisplayMode.BIT_DEPTH_MULTI &&
            mode2.getBitDepth() != DisplayMode.BIT_DEPTH_MULTI &&
            mode1.getBitDepth() != mode2.getBitDepth()) {
            return false;
        }
        if (mode1.getRefreshRate() != DisplayMode.REFRESH_RATE_UNKNOWN &&
            mode2.getRefreshRate() != DisplayMode.REFRESH_RATE_UNKNOWN &&
            mode1.getRefreshRate() != mode2.getRefreshRate()) {
            return false;
        }
        return true;
    }

    public void setFullScreen(DisplayMode displayMode) {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setIgnoreRepaint(true);
        frame.setResizable(false);

        device.setFullScreenWindow(frame);

        if (displayMode != null && device.isDisplayChangeSupported()) {
            try {
                device.setDisplayMode(displayMode);
            } catch (IllegalArgumentException ex) {
                // ignore
            }
            frame.setSize(displayMode.getWidth(), displayMode.getHeight());
        }

        try {
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    frame.createBufferStrategy(2);
                }
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            // ignore
        }
    }

    public Graphics2D getGraphics() {
        Window window = device.getFullScreenWindow();
        if (window != null) {
            BufferStrategy strategy = window.getBufferStrategy();
            return (Graphics2D) strategy.getDrawGraphics();
        }
        return null;
    }

    public void update() {
        Window window = device.getFullScreenWindow();
        if (window != null) {
            BufferStrategy strategy = window.getBufferStrategy();
            if (!strategy.contentsLost()) {
                strategy.show();
            }
        }
        Toolkit.getDefaultToolkit().sync();
    }

    public JFrame getFullScreenWindow() {
        return (JFrame) device.getFullScreenWindow();
    }

    public int getWidth() {
        Window window = device.getFullScreenWindow();
        if (window != null) {
            return window.getWidth();
        } else {
            return 0;
        }
    }

    public int getHeight() {
        Window window = device.getFullScreenWindow();
        if (window != null) {
            return window.getHeight();
        } else {
            return 0;
        }
    }

    public void restoreScreen() {
        Window window = device.getFullScreenWindow();
        if (window != null) {
            window.dispose();
        }
        device.setFullScreenWindow(null);
    }

    public BufferedImage createCompatibleImage(int w, int h, int transparency) {
        Window window = device.getFullScreenWindow();
        if (window != null) {
            GraphicsConfiguration gc = window.getGraphicsConfiguration();
            return gc.createCompatibleImage(w, h, transparency);
        }
        return null;
    }

    // Abstract decorator class
    public static abstract class ScreenManagerDecorator extends ScreenManager {
        protected ScreenManager decoratedScreenManager;

        public ScreenManagerDecorator(ScreenManager decoratedScreenManager) {
            this.decoratedScreenManager = decoratedScreenManager;
        }

        @Override
        public DisplayMode[] getCompatibleDisplayModes() {
            return decoratedScreenManager.getCompatibleDisplayModes();
        }

        @Override
        public DisplayMode findFirstCompatibleMode(DisplayMode[] modes) {
            return decoratedScreenManager.findFirstCompatibleMode(modes);
        }

        @Override
        public DisplayMode getCurrentDisplayMode() {
            return decoratedScreenManager.getCurrentDisplayMode();
        }

        @Override
        public boolean displayModesMatch(DisplayMode mode1, DisplayMode mode2) {
            return decoratedScreenManager.displayModesMatch(mode1, mode2);
        }

        @Override
        public void setFullScreen(DisplayMode displayMode) {
            decoratedScreenManager.setFullScreen(displayMode);
        }

        @Override
        public Graphics2D getGraphics() {
            return decoratedScreenManager.getGraphics();
        }

        @Override
        public void update() {
            decoratedScreenManager.update();
        }

        @Override
        public JFrame getFullScreenWindow() {
            return decoratedScreenManager.getFullScreenWindow();
        }

        @Override
        public int getWidth() {
            return decoratedScreenManager.getWidth();
        }

        @Override
        public int getHeight() {
            return decoratedScreenManager.getHeight();
        }

        @Override
        public void restoreScreen() {
            decoratedScreenManager.restoreScreen();
        }

        @Override
        public BufferedImage createCompatibleImage(int w, int h, int transparency) {
            return decoratedScreenManager.createCompatibleImage(w, h, transparency);
        }
    }

    // Concrete decorator class
    public static class LoggingScreenManagerDecorator extends ScreenManagerDecorator {
        public LoggingScreenManagerDecorator(ScreenManager decoratedScreenManager) {
            super(decoratedScreenManager);
        }

        @Override
        public void setFullScreen(DisplayMode displayMode) {
            System.out.println("Setting full screen mode: " + displayMode);
            super.setFullScreen(displayMode);
        }

        @Override
        public void restoreScreen() {
            System.out.println("Restoring screen to original mode.");
            super.restoreScreen();
        }

        @Override
        public void update() {
            System.out.println("Updating screen.");
            super.update();
        }

        @Override
        public Graphics2D getGraphics() {
            System.out.println("Getting graphics object.");
            return super.getGraphics();
        }
        
        // Add logging to other methods as needed
    }
}
