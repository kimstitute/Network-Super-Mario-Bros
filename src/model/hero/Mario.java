package model.hero;

import java.awt.*;
import java.awt.image.BufferedImage;
import manager.Camera;
import manager.GameEngine;
import model.GameObject;
import view.Animation;
import view.ImageLoader;

/**
 * 게임의 주인공 마리오 캐릭터를 구현하는 클래스
 * GameObject를 상속받아 위치, 속도, 물리 시뮬레이션 기능 확장
 * 마리오 폼 변환, 생명 관리, 아이템 획득 등 플레이어 관련 모든 기능 담당
 *
 * @author 네트워크프로그래밍 팀
 * @version 1.0
 * @since 2024-12-14
 */
public class Mario extends GameObject{

    /**
     * 마리오의 남은 생명 수
     * 0이 되면 게임 오버, 초기값은 3
     */
    private int remainingLives;
    
    /**
     * 마리오가 획득한 코인 수
     * 점수 계산과 1업 아이템 획득 조건에 사용됨
     */
    private int coins;
    
    /**
     * 마리오의 현재 점수
     * 적 처치, 아이템 획득 등으로 증가
     */
    private int points;
    
    /**
     * 무적 상태 지속 시간 (초 단위)
     * 아이템 획득 후 일정 시간 동안 무적 상태 유지
     */
    private double invincibilityTimer;
    
    /**
     * 마리오의 현재 폼 상태를 관리하는 MarioForm 객체
     * SMALL, NORMAL, FIRE, INVINCIBLE 등의 상태를 관리
     */
    private MarioForm marioForm;
    
    /**
     * 마리오가 바라보는 방향
     * true: 오른쪽, false: 왼쪽
     */
    private boolean toRight = true;

    /**
     * Mario 클래스 생성자
     * 마리오의 모든 기본 속성을 초기화하고 애니메이션 설정
     *
     * @param x 초기 X좌표
     * @param y 초기 Y좌표
     */
    public Mario(double x, double y){
        super(x, y, null);                    // 부모 클래스 생성자 호출
        setDimension(48,48);                 // 마리오 크기 설정 (48x48 픽셀)

        // 게임 상태 초기화
        remainingLives = 3;                   // 초기 생명 수
        points = 0;                          // 초기 점수
        coins = 0;                           // 초기 코인 수
        invincibilityTimer = 0;                // 무적 타이머 초기화

        // 이미지 로더와 애니메이션 초기화
        ImageLoader imageLoader = new ImageLoader();
        BufferedImage[] leftFrames = imageLoader.getLeftFrames(MarioForm.SMALL);   // 왼쪽 이동 프레임
        BufferedImage[] rightFrames = imageLoader.getRightFrames(MarioForm.SMALL); // 오른쪽 이동 프레임

        // 애니메이션 객체 생성 및 마리오 폼 초기화
        Animation animation = new Animation(leftFrames, rightFrames);
        marioForm = new MarioForm(animation, false, false);
        setStyle(marioForm.getCurrentStyle(toRight, false, false));  // 초기 스타일 설정
    }

    /**
     * 마리오를 화면에 그리는 메서드
     * 현재 이동 상태와 방향에 따라 적절한 스타일 이미지를 선택하여 렌더링
     *
     * @param g 그래픽스 컨텍스트 객체
     */
    @Override
    public void draw(Graphics g){
        boolean movingInX = (getVelX() != 0);  // 수평 이동 중인지 확인
        boolean movingInY = (getVelY() != 0);  // 수직 이동 중인지 확인

        // 이동 상태와 방향에 따른 스타일 이미지 선택
        setStyle(marioForm.getCurrentStyle(toRight, movingInX, movingInY));

        super.draw(g);  // 부모 클래스의 draw 메서드 호출
    }

    /**
     * 마리오가 점프하는 메서드
     * 바닥에 닿아있을 때만 점프 가능하며, 점프 사운드 재생
     *
     * @param engine 사운드 재생을 위한 GameEngine 참조
     */
    public void jump(GameEngine engine) {
        // 점프 중이 아니고 낙하 중이 아닐 때만 점프 가능
        if(!isJumping() && !isFalling()){
            setJumping(true);           // 점프 상태 활성화
            setVelY(10);              // 초기 점프 속도 설정
            engine.playJump();           // 점프 사운드 재생
        }
    }

    /**
     * 마리오를 좌우로 이동시키는 메서드
     * 카메라 위치를 고려하여 왼쪽 이동 제한
     *
     * @param toRight true면 오른쪽, false면 왼쪽으로 이동
     * @param camera 카메라 위치 정보 (왼쪽 이동 제한용)
     */
    public void move(boolean toRight, Camera camera) {
        if(toRight){
            setVelX(5);              // 오른쪽 이동 속도 설정
        }
        else if(camera.getX() < getX()){
            setVelX(-5);             // 왼쪽 이동 (카메라 왼쪽 경계 체크)
        }

        this.toRight = toRight;          // 현재 바라보는 방향 저장
    }

    /**
     * 적과 충돌했을 때 처리하는 메서드
     * 마리오 폼에 따라 다른 처리 (일반 폼: 사망, 슈퍼/파이어 폼: 무적)
     *
     * @param engine 사운드 재생과 카메라 효과를 위한 GameEngine 참조
     * @return true면 마리오 사망, false면 생존
     */
    public boolean onTouchEnemy(GameEngine engine){

        // 일반 폼(작음/보통)일 때만 적에게 피해를 입음
        if(!marioForm.isSuper() && !marioForm.isFire()){
            remainingLives--;           // 생명 감소
            engine.playMarioDies();       // 사망 사운드 재생
            return true;                // 마리오 사망 처리
        }
        else{
            // 슈퍼/파이어 폼일 때는 무적 상태가 됨
            engine.shakeCamera();                     // 화면 흔들림 효과
            marioForm = marioForm.onTouchEnemy(engine.getImageLoader());  // 폼 변환
            setDimension(48, 48);                     // 크기 재설정
            return false;                               // 마리오 생존
        }
    }

    /**
     * 파이어볼을 발사하는 메서드
     * 파이어 폼일 때만 파이어볼 발사 가능
     *
     * @return 생성된 Fireball 객체, 파이어 폼이 아니면 null
     */
    public Fireball fire(){
        return marioForm.fire(toRight, getX(), getY());
    }

    /**
     * 코인을 획득했을 때 호출되는 메서드
     * 코인 수를 1 증가시킴
     */
    public void acquireCoin() {
        coins++;
    }

    /**
     * 점수를 획득했을 때 호출되는 메서드
     *
     * @param point 획득한 점수
     */
    public void acquirePoints(int point){
        points = points + point;
    }

    /**
     * 남은 생명 수를 반환
     * @return 현재 남은 생명 수
     */
    public int getRemainingLives() {
        return remainingLives;
    }

    /**
     * 생명 수를 설정
     * 주로 게임 재시작이나 1업 아이템 획득 시 사용
     *
     * @param remainingLives 설정할 생명 수
     */
    public void setRemainingLives(int remainingLives) {
        this.remainingLives = remainingLives;
    }

    /**
     * 현재 점수를 반환
     * @return 현재 점수
     */
    public int getPoints() {
        return points;
    }

    /**
     * 현재 코인 수를 반환
     * @return 현재 코인 수
     */
    public int getCoins() {
        return coins;
    }

    /**
     * 현재 마리오 폼 객체를 반환
     * @return MarioForm 객체
     */
    public MarioForm getMarioForm() {
        return marioForm;
    }

    /**
     * 마리오 폼을 설정
     * 아이템 획득 시 폼 변환에 사용
     *
     * @param marioForm 설정할 MarioForm 객체
     */
    public void setMarioForm(MarioForm marioForm) {
        this.marioForm = marioForm;
    }

    /**
     * 현재 슈퍼 폼인지 확인
     * @return 슈퍼 폼이면 true, 아니면 false
     */
    public boolean isSuper() {
        return marioForm.isSuper();
    }

    /**
     * 현재 바라보는 방향을 반환
     * @return true면 오른쪽, false면 왼쪽
     */
    public boolean getToRight() {
        return toRight;
    }

    /**
     * 마리오의 위치를 초기 상태로 리셋
     * 주로 게임 재시작이나 생명 소진 후 재시작 시 사용
     */
    public void resetLocation() {
        setVelX(0);              // 수평 속도 초기화
        setVelY(0);              // 수직 속도 초기화
        setX(50);               // 초기 X좌표 설정
        setJumping(false);         // 점프 상태 초기화
        setFalling(true);          // 낙하 상태 활성화
    }
}
