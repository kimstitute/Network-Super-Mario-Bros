package model.enemy;

import model.GameObject;
import java.awt.image.BufferedImage;

// 적 기본 클래스: 굼바, 쿠파 트루파 등의 부모 클래스
public abstract class Enemy extends GameObject{

    public Enemy(double x, double y, BufferedImage style) {
        super(x, y, style);
        setFalling(false);
        setJumping(false);
    }
}
