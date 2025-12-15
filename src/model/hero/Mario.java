package model.hero;

import java.awt.*;
import java.awt.image.BufferedImage;
import manager.Camera;
import manager.GameEngine;
import model.GameObject;

// 마리오 캐릭터: 위치, 속도, 폼 변환, 생명 관리, 충돌 처리
public class Mario extends GameObject{

    private int remainingLives; // 남은 생명 (0이면 게임 오버)
    private int coins; // 획득한 코인 수
    private int points; // 현재 점수
    private double invincibilityTimer; // 별 아이템 무적 시간 (초)
    private double damageInvincibilityTimer; // 피격 후 무적 시간 (초)
    private MarioForm marioForm; // 현재 폼 상태 (Small/Super/Fire)
    private boolean toRight = true; // 바라보는 방향

    public Mario(double x, double y){
        this(x, y, null);
    }
    
    public Mario(double x, double y, view.ImageLoader imageLoader){
        super(x, y, null);
        setDimension(48,48);

        remainingLives = 3;
        points = 0;
        coins = 0;
        invincibilityTimer = 0;
        damageInvincibilityTimer = 0;

        if (imageLoader != null) {
            view.Animation animation = new view.Animation(imageLoader.getLeftFrames(1), imageLoader.getRightFrames(1));
            marioForm = new MarioForm(animation, false, false, imageLoader);
        } else {
            marioForm = new MarioForm(null, false, false);
        }
        setStyle(null);
    }

    @Override
    public void draw(Graphics g){
        boolean movingInX = (getVelX() != 0);
        boolean movingInY = (getVelY() != 0);

        BufferedImage currentStyle = marioForm.getCurrentStyle(toRight, movingInX, movingInY);
        
        if (currentStyle == null) {
            System.out.println("[MARIO] Style is NULL! Animation not initialized.");
        }
        
        setStyle(currentStyle);
        super.draw(g);
    }

    // 점프 (바닥에 있을 때만 가능)
    public void jump(GameEngine engine) {
        if(!isJumping() && !isFalling()){
            setJumping(true);
            setVelY(10);
            if (engine != null) {
                engine.playJump();
            }
        }
    }

    // 좌우 이동 (카메라 왼쪽 경계를 넘어가지 않도록 제한)
    public void move(boolean toRight, Camera camera) {
        if(toRight){
            setVelX(5);
        }
        else if(camera == null || camera.getX() < getX()){
            setVelX(-5);
        }

        this.toRight = toRight;
    }

    // 적과 충돌 시 처리: 피격 무적 상태면 무시, 아니면 폼 변환 또는 생명 감소
    public boolean onTouchEnemy(GameEngine engine){
        if (isDamageInvincible()) {
            return false;
        }

        if(!marioForm.isSuper() && !marioForm.isFire()){
            remainingLives--;
            damageInvincibilityTimer = 2.0; // 2초 피격 무적
            if (engine != null) {
                engine.playMarioDies();
            }
            return true; // 마리오 사망
        }
        else{
            damageInvincibilityTimer = 2.0; // 2초 피격 무적
            if (engine != null) {
                engine.shakeCamera();
                marioForm = marioForm.onTouchEnemy(engine.getImageLoader());
            }
            setDimension(48, 48);
            return false; // 마리오 생존
        }
    }

    // 파이어볼 발사 (파이어 폼일 때만 가능)
    public Fireball fire(){
        return marioForm.fire(toRight, getX(), getY());
    }

    public void acquireCoin() {
        coins++;
    }

    public void acquirePoints(int point){
        points = points + point;
    }

    public int getRemainingLives() {
        return remainingLives;
    }

    public void setRemainingLives(int remainingLives) {
        this.remainingLives = remainingLives;
    }

    public int getPoints() {
        return points;
    }

    public int getCoins() {
        return coins;
    }

    public MarioForm getMarioForm() {
        return marioForm;
    }

    public void setMarioForm(MarioForm marioForm) {
        this.marioForm = marioForm;
    }

    public boolean isSuper() {
        return marioForm.isSuper();
    }

    public boolean getToRight() {
        return toRight;
    }

    public double getDamageInvincibilityTimer() {
        return damageInvincibilityTimer;
    }

    public void setDamageInvincibilityTimer(double time) {
        this.damageInvincibilityTimer = time;
    }

    public boolean isDamageInvincible() {
        return damageInvincibilityTimer > 0;
    }

    // 피격 무적 타이머 감소 (서버에서 매 프레임 호출)
    public void updateDamageInvincibility(double delta) {
        if (damageInvincibilityTimer > 0) {
            damageInvincibilityTimer -= delta;
            if (damageInvincibilityTimer < 0) {
                damageInvincibilityTimer = 0;
            }
        }
    }

    // 마리오 위치를 초기 위치로 리셋 (리스폰 시 사용)
    public void resetLocation() {
        System.out.println("[Mario] Resetting location - Lives remaining: " + remainingLives);
        setVelX(0);
        setVelY(0);
        setX(50);
        setY(350);
        setJumping(false);
        setFalling(true);
    }
}
