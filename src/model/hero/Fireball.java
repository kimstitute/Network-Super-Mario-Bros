package model.hero;

import model.GameObject;
import java.awt.image.BufferedImage;

// 파이어볼: Fire Mario가 발사하는 투사체, 수평으로 날아가 적을 처치
public class Fireball extends GameObject {

    public Fireball(double x, double y, BufferedImage style, boolean toRight) {
        super(x, y, style);
        setDimension(24, 24);
        setFalling(false);
        setJumping(false);
        setVelX(10);

        if(!toRight)
            setVelX(-5);
    }
}
