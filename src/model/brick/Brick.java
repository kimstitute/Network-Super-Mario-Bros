package model.brick;

import manager.GameEngine;
import model.GameObject;
import model.Map;
import model.prize.Prize;

import java.awt.image.BufferedImage;

// 블록 기본 클래스: 일반 블록, 물음표 블록, 지면, 파이프의 부모 클래스
public abstract class Brick extends GameObject{

    private boolean breakable; // 부술 수 있는지 (일반 블록)
    private boolean empty; // 아이템이 비었는지 (물음표 블록)

    public Brick(double x, double y, BufferedImage style){
        super(x, y, style);
        setDimension(48, 48);
    }

    public boolean isBreakable() {
        return breakable;
    }

    public void setBreakable(boolean breakable) {
        this.breakable = breakable;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public Prize reveal(GameEngine engine){ return null;}

    public Prize getPrize() {
        return null;
    }
}
