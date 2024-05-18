package com.TETOSOFT.tilegame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import com.TETOSOFT.graphics.*;
import com.TETOSOFT.input.*;
import com.TETOSOFT.test.GameCore;
import com.TETOSOFT.tilegame.sprites.*;

public class GameEngine extends GameCore {
    
    public static void main(String[] args) {
        new GameEngine().run();
    }
    
    public static final float GRAVITY = 0.002f;

    private Point pointCache = new Point();
    private TileMap map;
    private MapLoader mapLoader;
    private InputManager inputManager;
    private TileMapDrawer drawer;
    
    private GameAction moveLeft;
    private GameAction moveRight;
    private GameAction jump;
    private GameAction exit;
    private int collectedStars = 0;
    private int numLives = 6;
   
    private ScreenManager screenManager;

    @Override
    public void init() {
        super.init();

        // Decorate the ScreenManager with logging functionality
        screenManager = new ScreenManager.LoggingScreenManagerDecorator(ScreenManager.getInstance());

        // set up input manager
        initInput();
        
        // start resource manager
        mapLoader = new MapLoader(screenManager.getFullScreenWindow().getGraphicsConfiguration());
        
        // load resources
        drawer = new TileMapDrawer();
        drawer.setBackground(mapLoader.loadImage("background.jpg"));
        
        // load first map
        map = mapLoader.loadNextMap();
    }
    
    @Override
    public void stop() {
        super.stop();
    }
    
   private void initInput() {

    // Create the GameAction instances
    moveLeft = new GameAction("moveLeft");
    moveRight = new GameAction("moveRight");
    jump = new GameAction("jump", GameAction.DETECT_INITAL_PRESS_ONLY);
    exit = new GameAction("exit", GameAction.DETECT_INITAL_PRESS_ONLY);

    // Set the initial states for the GameAction instances
    moveLeft.press(0); 
    moveRight.press(0); 
    jump.press(0); 
    exit.press(0);
    
    inputManager = new InputManager(screen.getFullScreenWindow());
    inputManager.setCursor(InputManager.INVISIBLE_CURSOR);
    
    // Map the GameAction instances to the corresponding keys
    inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
    inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);
    inputManager.mapToKey(jump, KeyEvent.VK_SPACE);
    inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
}
   
    
    private void checkInput(long elapsedTime) {
        if (exit.isPressed()) {
            stop();
        }
        
        Player player = (Player)map.getPlayer();
        if (player.isAlive()) {
            float velocityX = 0;
            if (moveLeft.isPressed()) {
                velocityX -= player.getMaxSpeed();
            }
            if (moveRight.isPressed()) {
                velocityX += player.getMaxSpeed();
            }
            if (jump.isPressed()) {
                player.jump(false);
            }
            player.setVelocityX(velocityX);
        }
    }
    
    @Override
    public void draw(Graphics2D g) {
        drawer.draw(g, map, screenManager.getWidth(), screenManager.getHeight());
        g.setColor(Color.WHITE);
        g.drawString("Press ESC for EXIT.", 10.0f, 20.0f);
        g.setColor(Color.GREEN);
        g.drawString("Coins: " + collectedStars, 300.0f, 20.0f);
        g.setColor(Color.YELLOW);
        g.drawString("Lives: " + numLives, 500.0f, 20.0f);
        g.setColor(Color.WHITE);
        g.drawString("Home: " + mapLoader.currentMap, 700.0f, 20.0f);
    }
    
    public TileMap getMap() {
        return map;
    }
    
    public Point getTileCollision(Sprite sprite, float newX, float newY) {
        float fromX = Math.min(sprite.getX(), newX);
        float fromY = Math.min(sprite.getY(), newY);
        float toX = Math.max(sprite.getX(), newX);
        float toY = Math.max(sprite.getY(), newY);
        
        int fromTileX = TileMapDrawer.pixelsToTiles(fromX);
        int fromTileY = TileMapDrawer.pixelsToTiles(fromY);
        int toTileX = TileMapDrawer.pixelsToTiles(toX + sprite.getWidth() - 1);
        int toTileY = TileMapDrawer.pixelsToTiles(toY + sprite.getHeight() - 1);
        
        for (int x = fromTileX; x <= toTileX; x++) {
            for (int y = fromTileY; y <= toTileY; y++) {
                if (x < 0 || x >= map.getWidth() || map.getTile(x, y) != null) {
                    pointCache.setLocation(x, y);
                    return pointCache;
                }
            }
        }
        
        return null;
    }
    
    public boolean isCollision(Sprite s1, Sprite s2) {
        if (s1 == s2) {
            return false;
        }
        
        if (s1 instanceof Creature && !((Creature)s1).isAlive()) {
            return false;
        }
        if (s2 instanceof Creature && !((Creature)s2).isAlive()) {
            return false;
        }
        
        int s1x = Math.round(s1.getX());
        int s1y = Math.round(s1.getY());
        int s2x = Math.round(s2.getX());
        int s2y = Math.round(s2.getY());
        
        return (s1x < s2x + s2.getWidth() &&
                s2x < s1x + s1.getWidth() &&
                s1y < s2y + s2.getHeight() &&
                s2y < s1y + s1.getHeight());
    }
    
    public Sprite getSpriteCollision(Sprite sprite) {
        Iterator<Sprite> i = map.getSprites();
        while (i.hasNext()) {
            Sprite otherSprite = i.next();
            if (isCollision(sprite, otherSprite)) {
                return otherSprite;
            }
        }
        return null;
    }
    
    @Override
    public void update(long elapsedTime) {
        Creature player = (Creature)map.getPlayer();
        
        if (player.getState() == Creature.STATE_DEAD) {
            map = mapLoader.reloadMap();
            return;
        }
        
        checkInput(elapsedTime);
        
        updateCreature(player, elapsedTime);
        player.update(elapsedTime);
        
        Iterator<Sprite> i = map.getSprites();
        while (i.hasNext()) {
            Sprite sprite = i.next();
            if (sprite instanceof Creature) {
                Creature creature = (Creature)sprite;
                if (creature.getState() == Creature.STATE_DEAD) {
                    i.remove();
                } else {
                    updateCreature(creature, elapsedTime);
                }
            }
            sprite.update(elapsedTime);
        }
    }
    
    private void updateCreature(Creature creature, long elapsedTime) {
        if (!creature.isFlying()) {
            creature.setVelocityY(creature.getVelocityY() + GRAVITY * elapsedTime);
        }
        
        float dx = creature.getVelocityX();
        float oldX = creature.getX();
        float newX = oldX + dx * elapsedTime;
        Point tile = getTileCollision(creature, newX, creature.getY());
        if (tile == null) {
            creature.setX(newX);
        } else {
            if (dx > 0) {
                creature.setX(TileMapDrawer.tilesToPixels(tile.x) - creature.getWidth());
            } else if (dx < 0) {
                creature.setX(TileMapDrawer.tilesToPixels(tile.x + 1));
            }
            creature.collideHorizontal();
        }
        if (creature instanceof Player) {
            checkPlayerCollision((Player)creature, false);
        }
        
        float dy = creature.getVelocityY();
        float oldY = creature.getY();
        float newY = oldY + dy * elapsedTime;
        tile = getTileCollision(creature, creature.getX(), newY);
        if (tile == null) {
            creature.setY(newY);
        } else {
            if (dy > 0) {
                creature.setY(TileMapDrawer.tilesToPixels(tile.y) - creature.getHeight());
            } else if (dy < 0) {
                creature.setY(TileMapDrawer.tilesToPixels(tile.y + 1));
            }
            creature.collideVertical();
        }
        if (creature instanceof Player) {
            boolean canKill = (oldY < creature.getY());
            checkPlayerCollision((Player)creature, canKill);
        }
    }
    
    public void checkPlayerCollision(Player player, boolean canKill) {
        if (!player.isAlive()) {
            return;
        }
        
        Sprite collisionSprite = getSpriteCollision(player);
        if (collisionSprite instanceof PowerUp) {
            acquirePowerUp((PowerUp)collisionSprite);
        } else if (collisionSprite instanceof Creature) {
            Creature badguy = (Creature)collisionSprite;
            if (canKill) {
                badguy.setState(Creature.STATE_DYING);
                player.setY(badguy.getY() - player.getHeight());
                player.jump(true);
            } else {
                player.setState(Creature.STATE_DYING);
                numLives--;
                if (numLives <= 0) {
                    stop();
                }
            }
        }
    }

  /**
     * Gives the player the speicifed power up and removes it
     * from the map.
     * @param powerUp
     */
    public void acquirePowerUp(PowerUp powerUp) {
        // remove it from the map
        map.removeSprite(powerUp);
        
        if (powerUp instanceof PowerUp.PowerUpFactory.Star) {
            // do something here, like give the player points
            collectedStars++;
            if(collectedStars==100) 
            {
                numLives++;
                collectedStars=0;
            }
            
        } else if (powerUp instanceof PowerUp.PowerUpFactory.Music) {
            // change the music
            
        } else if (powerUp instanceof PowerUp.PowerUpFactory.Goal) {
            // advance to next map      
            map = mapLoader.loadNextMap();
        }
    }  
}