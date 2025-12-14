package model;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 게임 내 모든 오브젝트의 기본 클래스 (추상 클래스)
 * 위치, 속도, 크기, 이미지 등 모든 게임 오브젝트의 공통 속성을 정의
 * 마리오, 적, 아이템, 블록 등 모든 게임 요소가 상속받음
 *
 * @author 네트워크프로그래밍 팀
 * @version 1.0
 * @since 2024-12-14
 */
public abstract class GameObject {

    /**
     * 오브젝트의 현재 X좌표 (게임 월드 기준)
     * 화면 왼쪽 상단을 (0, 0)으로 하는 좌표계
     */
    private double x, y;

    /**
     * 오브젝트의 현재 속도 (X, Y축 방향)
     * 프레임마다 이동할 거리를 저장
     */
    private double velX, velY;

    /**
     * 오브젝트의 크기 (너비, 높이)
     * 충돌 감지와 렌더링에 사용됨
     */
    private Dimension dimension;

    /**
     * 오브젝트의 현재 스타일 이미지
     * 렌더링할 때 사용되는 BufferedImage 객체
     */
    private BufferedImage style;

    /**
     * 중력 가속도
     * 점프 후 낙하할 때 속도 증가량 (0.38 = 마리오 게임의 중력값)
     */
    private double gravityAcc;

    /**
     * 낙하 상태 플래그
     * true일 때 아래로 떨어지고 있음을 의미
     */
    private boolean falling, jumping;

    /**
     * GameObject 생성자
     * 모든 게임 오브젝트의 기본 속성을 초기화
     *
     * @param x 초기 X좌표
     * @param y 초기 Y좌표
     * @param style 오브젝트의 스타일 이미지
     */
    public GameObject(double x, double y, BufferedImage style){
        setLocation(x, y);                    // 위치 설정
        setStyle(style);                        // 스타일 이미지 설정

        // 스타일 이미지가 있을 경우 크기를 자동으로 설정
        if(style != null){
            setDimension(style.getWidth(), style.getHeight());
        }

        // 초기 속도와 물리 상태 설정
        setVelX(0);                           // 초기 수평 속도 0
        setVelY(0);                           // 초기 수직 속도 0
        setGravityAcc(0.38);                    // 중력 가속도 설정
        jumping = false;                         // 점프 상태 비활성화
        falling = true;                          // 낙하 상태 활성화 (바닥에 없음)
    }

    /**
     * 오브젝트를 화면에 그리는 메서드
     * 현재 위치에 스타일 이미지를 렌더링
     *
     * @param g 그래픽스 컨텍스트 객체
     */
    public void draw(Graphics g) {
        BufferedImage style = getStyle();

        // 스타일 이미지가 있을 경우에만 렌더링
        if(style != null){
            g.drawImage(style, (int)x, (int)y, null);
        }

        // 디버깅용 충돌 박스 표시 (현재는 주석 처리)
        /*Graphics2D g2 = (Graphics2D)g;
        g2.setColor(Color.WHITE);

        g2.draw(getTopBounds());       // 위쪽 충돌 박스
        g2.draw(getBottomBounds());    // 아래쪽 충돌 박스
        g2.draw(getRightBounds());     // 오른쪽 충돌 박스
        g2.draw(getLeftBounds());      // 왼쪽 충돌 박스*/
    }

    /**
     * 오브젝트의 위치와 물리 상태를 업데이트하는 메서드
     * 중력, 점프, 낙하, 이동을 모두 처리하는 게임 루프의 핵심
     */
    public void updateLocation() {
        // 점프 중이고 수직 속도가 0 이하가 되면 점프 종료
        if(jumping && velY <= 0){
            jumping = false;      // 점프 상태 종료
            falling = true;       // 낙하 상태 시작
        }
        // 점프 중일 때는 위쪽으로 가속
        else if(jumping){
            velY = velY - gravityAcc;  // 중력으로 인한 속도 감소
            y = y - velY;              // 위쪽으로 이동
        }

        // 낙하 중일 때는 아래쪽으로 가속
        if(falling){
            y = y + velY;              // 아래쪽으로 이동
            velY = velY + gravityAcc;  // 중력으로 인한 속도 증가
        }

        // 수평 이동 (중력과 무관)
        x = x + velX;
    }

    /**
     * 오브젝트의 위치를 설정하는 메서드
     * X, Y좌표를 동시에 설정
     *
     * @param x 설정할 X좌표
     * @param y 설정할 Y좌표
     */
    public void setLocation(double x, double y) {
        setX(x);
        setY(y);
    }

    /**
     * 오브젝트의 현재 X좌표를 반환
     * @return 현재 X좌표 (double)
     */
    public double getX() {
        return x;
    }

    /**
     * 오브젝트의 X좌표를 설정
     * @param x 설정할 X좌표 값
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * 오브젝트의 현재 Y좌표를 반환
     * @return 현재 Y좌표 (double)
     */
    public double getY() {
        return y;
    }

    /**
     * 오브젝트의 Y좌표를 설정
     * @param y 설정할 Y좌표 값
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * 오브젝트의 크기(너비, 높이)를 반환
     * @return Dimension 객체로 표현된 크기
     */
    public Dimension getDimension(){
        return dimension;
    }

    /**
     * 오브젝트의 크기를 설정
     * @param dimension 설정할 Dimension 객체
     */
    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    /**
     * 너비와 높이로 크기를 설정하는 간편 메서드
     * @param width 설정할 너비
     * @param height 설정할 높이
     */
    public void setDimension(int width, int height){ this.dimension = new Dimension(width, height); }

    /**
     * 오브젝트의 현재 스타일 이미지를 반환
     * @return BufferedImage 객체로 표현된 스타일
     */
    public BufferedImage getStyle() {
        return style;
    }

    /**
     * 오브젝트의 스타일 이미지를 설정
     * @param style 설정할 BufferedImage 객체
     */
    public void setStyle(BufferedImage style) {
        this.style = style;
    }

    /**
     * 오브젝트의 수평 속도를 반환
     * @return 현재 수평 속도 (double)
     */
    public double getVelX() {
        return velX;
    }

    /**
     * 오브젝트의 수평 속도를 설정
     * @param velX 설정할 수평 속도 값
     */
    public void setVelX(double velX) {
        this.velX = velX;
    }

    /**
     * 오브젝트의 수직 속도를 반환
     * @return 현재 수직 속도 (double)
     */
    public double getVelY() {
        return velY;
    }

    /**
     * 오브젝트의 수직 속도를 설정
     * @param velY 설정할 수직 속도 값
     */
    public void setVelY(double velY) {
        this.velY = velY;
    }

    /**
     * 중력 가속도를 반환
     * @return 현재 중력 가속도 값 (double)
     */
    public double getGravityAcc() {
        return gravityAcc;
    }

    /**
     * 중력 가속도를 설정
     * @param gravityAcc 설정할 중력 가속도 값
     */
    public void setGravityAcc(double gravityAcc) {
        this.gravityAcc = gravityAcc;
    }

    /**
     * 오브젝트의 위쪽 충돌 박스를 반환
     * 상단 1/3 영역의 충돌 감지에 사용
     * @return Rectangle 객체로 표현된 충돌 박스
     */
    public Rectangle getTopBounds(){
        return new Rectangle((int)x+dimension.width/6, (int)y, 2*dimension.width/3, dimension.height/2);
    }

    /**
     * 오브젝트의 아래쪽 충돌 박스를 반환
     * 하단 1/2 영역의 충돌 감지에 사용
     * @return Rectangle 객체로 표현된 충돌 박스
     */
    public Rectangle getBottomBounds(){
        return new Rectangle((int)x+dimension.width/6, (int)y + dimension.height/2, 2*dimension.width/3, dimension.height/2);
    }

    /**
     * 오브젝트의 왼쪽 충돌 박스를 반환
     * 좌측 1/4 영역의 충돌 감지에 사용
     * @return Rectangle 객체로 표현된 충돌 박스
     */
    public Rectangle getLeftBounds(){
        return new Rectangle((int)x, (int)y + dimension.height/4, dimension.width/4, dimension.height/2);
    }

    /**
     * 오브젝트의 오른쪽 충돌 박스를 반환
     * 우측 1/4 영역의 충돌 감지에 사용
     * @return Rectangle 객체로 표현된 충돌 박스
     */
    public Rectangle getRightBounds(){
        return new Rectangle((int)x + 3*dimension.width/4, (int)y + dimension.height/4, dimension.width/4, dimension.height/2);
    }

    /**
     * 오브젝트의 전체 충돌 박스를 반환
     * 오브젝트의 전체 영역을 나타내는 사각형
     * @return Rectangle 객체로 표현된 전체 충돌 박스
     */
    public Rectangle getBounds(){
        return new Rectangle((int)x, (int)y, dimension.width, dimension.height);
    }

    /**
     * 오브젝트의 낙하 상태를 반환
     * @return 낙하 중이면 true, 아니면 false
     */
    public boolean isFalling() {
        return falling;
    }

    /**
     * 오브젝트의 낙하 상태를 설정
     * @param falling 낙하 상태로 설정할 true/false 값
     */
    public void setFalling(boolean falling) {
        this.falling = falling;
    }

    /**
     * 오브젝트의 점프 상태를 반환
     * @return 점프 중이면 true, 아니면 false
     */
    public boolean isJumping() {
        return jumping;
    }

    /**
     * 오브젝트의 점프 상태를 설정
     * @param jumping 점프 상태로 설정할 true/false 값
     */
    public void setJumping(boolean jumping) {
        this.jumping = jumping;
    }
}
