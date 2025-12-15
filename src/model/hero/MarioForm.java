package model.hero;

import java.awt.image.BufferedImage;
import view.Animation;
import view.ImageLoader;

// 마리오 폼 상태 관리: Small, Super, Fire
public class MarioForm {

    public static final int SMALL = 0, SUPER = 1, FIRE = 2;

    private Animation animation;
    private boolean isSuper, isFire;
    private BufferedImage fireballStyle;

    public MarioForm(Animation animation, boolean isSuper, boolean isFire){
        this(animation, isSuper, isFire, null);
    }

    public MarioForm(Animation animation, boolean isSuper, boolean isFire, ImageLoader imageLoader){
        this.animation = animation;
        this.isSuper = isSuper;
        this.isFire = isFire;

        if (animation != null) {
            if (imageLoader == null) {
                imageLoader = new ImageLoader();
            }
            BufferedImage fireball = imageLoader.loadImage("/sprite.png");
            fireballStyle = imageLoader.getSubImage(fireball, 3, 4, 24, 24);
        }
    }

    // 현재 상태에 맞는 스프라이트 반환: 방향, 이동, 점프 고려
    public BufferedImage getCurrentStyle(boolean toRight, boolean movingInX, boolean movingInY){
        if (animation == null) {
            return null;
        }

        BufferedImage style;

        if(movingInY && toRight){
            style = animation.getRightFrames()[0];
        }
        else if(movingInY){
            style = animation.getLeftFrames()[0];
        }
        else if(movingInX){
            style = animation.animate(5, toRight);
        }
        else {
            if(toRight){
                style = animation.getRightFrames()[1];
            }
            else
                style = animation.getLeftFrames()[1];
        }

        return style;
    }

    // 적과 충돌 시 폼 변환: Fire → Super → Small
    public MarioForm onTouchEnemy(ImageLoader imageLoader) {
        if (isFire) {
            Animation superAnimation = new Animation(
                imageLoader.getRightFrames(1),
                imageLoader.getLeftFrames(1)
            );
            return new MarioForm(superAnimation, true, false, imageLoader);
        } else if (isSuper) {
            Animation smallAnimation = new Animation(
                imageLoader.getRightFrames(0),
                imageLoader.getLeftFrames(0)
            );
            return new MarioForm(smallAnimation, false, false, imageLoader);
        }
        return this;
    }

    // 파이어볼 발사 (Fire Mario만 가능)
    public Fireball fire(boolean toRight, double x, double y) {
        if(isFire){
            return new Fireball(x, y + 48, fireballStyle, toRight);
        }
        return null;
    }

    public boolean isSuper() {
        return isSuper;
    }

    public void setSuper(boolean aSuper) {
        isSuper = aSuper;
    }

    public boolean isFire() {
        return isFire;
    }
}
