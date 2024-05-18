package com.TETOSOFT.tilegame.sprites;

import com.TETOSOFT.graphics.Animation;
import com.TETOSOFT.graphics.Sprite;

/**
 * A PowerUp class is a Sprite that the player can pick up.
 */
public abstract class PowerUp extends Sprite {

    public PowerUp(Animation anim) {
        super(anim);
    }

    public Object clone() {
        try {
            return getClass().getConstructor(Animation.class).newInstance(anim.clone());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * A factory class for creating PowerUp instances.
     */
    public static class PowerUpFactory {
          public static PowerUp createPowerUp(String type, Animation anim) {
            if (type.equalsIgnoreCase("Star")) {
                return new Star(anim);
            } else if (type.equalsIgnoreCase("Music")) {
                return new Music(anim);
            } else if (type.equalsIgnoreCase("Goal")) {
                return new Goal(anim);
            }
            return null;
        }
    
        /**
        * A Star PowerUp. Gives the player points.
        */
       public static class Star extends PowerUp {
           public Star(Animation anim) {
               super(anim);
           }
       }

       /**
        * A Music PowerUp. Changes the game music.
        */
       public static class Music extends PowerUp {
           public Music(Animation anim) {
               super(anim);
           }
       }

       /**
        * A Goal PowerUp. Advances to the next map.
        */
       public static class Goal extends PowerUp {
           public Goal(Animation anim) {
               super(anim);
           }
       }
    }
}