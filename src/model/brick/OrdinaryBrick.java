package model.brick;

import manager.GameEngine;
import manager.MapManager;
import model.Map;
import model.prize.Prize;
import view.Animation;
import view.ImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;

public class OrdinaryBrick extends Brick {

    private Animation animation;
    private boolean breaking;
    private int frames;
    private boolean animationLoaded = false;

    public OrdinaryBrick(double x, double y, BufferedImage style){
        super(x, y, style);
        setBreakable(true);
        setEmpty(true);
        breaking = false;
    }

    private void ensureAnimationLoaded(){
        if (!animationLoaded) {
            ImageLoader imageLoader = new ImageLoader();
            BufferedImage[] leftFrames = imageLoader.getBrickFrames();
            animation = new Animation(leftFrames, leftFrames);
            if (animation != null && animation.getLeftFrames() != null) {
                frames = animation.getLeftFrames().length;
            }
            animationLoaded = true;
        }
    }

    @Override
    public Prize reveal(GameEngine engine){
        if (engine == null) {
            return null;
        }
        
        MapManager manager = engine.getMapManager();
        if(!manager.getMario().isSuper())
            return null;

        breaking = true;
        manager.addRevealedBrick(this);

        double newX = getX() - 27, newY = getY() - 27;
        setLocation(newX, newY);

        return null;
    }

    public int getFrames(){
        return frames;
    }

    public void animate(){
        if(breaking){
            ensureAnimationLoaded();
            setStyle(animation.animate(3, true));
            frames--;
        }
    }
}
