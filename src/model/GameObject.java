package model;

import java.awt.*;
import java.awt.image.BufferedImage;

// 게임 오브젝트 기본 클래스: 위치, 속도, 크기, 물리, 충돌 박스 관리
public abstract class GameObject {

    private double x, y; // 위치
    private double velX, velY; // 속도
    private Dimension dimension; // 크기
    private BufferedImage style; // 스프라이트 이미지
    private double gravityAcc; // 중력 가속도 (0.38)
    private boolean falling, jumping; // 물리 상태

    public GameObject(double x, double y, BufferedImage style){
        setLocation(x, y);
        setStyle(style);

        if(style != null){
            setDimension(style.getWidth(), style.getHeight());
        }

        setVelX(0);
        setVelY(0);
        setGravityAcc(0.38);
        jumping = false;
        falling = true;
    }

    // 스프라이트 렌더링
    public void draw(Graphics g) {
        BufferedImage style = getStyle();

        if(style != null){
            g.drawImage(style, (int)x, (int)y, null);
        }
    }

    // 물리 업데이트: 중력, 점프, 낙하, 수평 이동
    public void updateLocation() {
        if(jumping && velY <= 0){
            jumping = false;
            falling = true;
        }
        else if(jumping){
            velY = velY - gravityAcc;
            y = y - velY;
        }

        if(falling){
            y = y + velY;
            velY = velY + gravityAcc;
        }

        x = x + velX;
    }

    public void setLocation(double x, double y) {
        setX(x);
        setY(y);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Dimension getDimension(){
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public void setDimension(int width, int height){ 
        this.dimension = new Dimension(width, height); 
    }

    public BufferedImage getStyle() {
        return style;
    }

    public void setStyle(BufferedImage style) {
        this.style = style;
    }

    public double getVelX() {
        return velX;
    }

    public void setVelX(double velX) {
        this.velX = velX;
    }

    public double getVelY() {
        return velY;
    }

    public void setVelY(double velY) {
        this.velY = velY;
    }

    public double getGravityAcc() {
        return gravityAcc;
    }

    public void setGravityAcc(double gravityAcc) {
        this.gravityAcc = gravityAcc;
    }

    // 충돌 박스: 오브젝트를 상하좌우 영역으로 나누어 정밀한 충돌 감지
    public Rectangle getTopBounds(){
        return new Rectangle((int)x+dimension.width/6, (int)y, 2*dimension.width/3, dimension.height/2);
    }

    public Rectangle getBottomBounds(){
        return new Rectangle((int)x+dimension.width/6, (int)y + dimension.height/2, 2*dimension.width/3, dimension.height/2);
    }

    public Rectangle getLeftBounds(){
        return new Rectangle((int)x, (int)y + dimension.height/4, dimension.width/4, dimension.height/2);
    }

    public Rectangle getRightBounds(){
        return new Rectangle((int)x + 3*dimension.width/4, (int)y + dimension.height/4, dimension.width/4, dimension.height/2);
    }

    public Rectangle getBounds(){
        return new Rectangle((int)x, (int)y, dimension.width, dimension.height);
    }

    public boolean isFalling() {
        return falling;
    }

    public void setFalling(boolean falling) {
        this.falling = falling;
    }

    public boolean isJumping() {
        return jumping;
    }

    public void setJumping(boolean jumping) {
        this.jumping = jumping;
    }
}
