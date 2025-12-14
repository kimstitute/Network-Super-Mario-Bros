package manager;

/**
 * 게임 화면의 카메라 위치와 시각 효과를 관리하는 클래스
 * 마리오의 이동에 따라 화면을 스크롤하고, 특정 이벤트 시 화면 흔들림 효과를 제공
 * 2D 사이드 스크롤링 게임에서 필수적인 뷰포트 관리 기능을 담당
 *
 * @author 네트워크프로그래밍 팀
 * @version 1.0
 * @since 2024-12-14
 */
public class Camera {

    /**
     * 카메라의 현재 X좌표 - 화면 왼쪽 상단의 월드 좌표
     * 마리오가 오른쪽으로 이동할 때 증가하며, 화면 스크롤을 담당
     */
    private double x;
    
    /**
     * 카메라의 현재 Y좌표 - 화면 위쪽 상단의 월드 좌표
     * 현재 구현에서는 고정되어 있으나, 수직 스크롤 확장성을 위해 유지
     */
    private double y;
    
    /**
     * 화면 흔들림 효과의 지속 프레임 수
     * 흔들림 효과가 활성화되었을 때 남은 프레임 수를 카운트
     */
    private int frameNumber;
    
    /**
     * 화면 흔들림 효과 활성화 상태
     * true일 때 화면이 좌우로 흔들리며, 주로 마리오가 적을 밟았을 때 사용
     */
    private boolean shaking;

    /**
     * 카메라 클래스의 생성자
     * 초기 위치를 (0, 0)으로 설정하고 흔들림 효과를 비활성화 상태로 초기화
     */
    public Camera(){
        this.x = 0;                    // 초기 X좌표를 0으로 설정
        this.y = 0;                    // 초기 Y좌표를 0으로 설정
        this.frameNumber = 25;           // 흔들림 지속 프레임 초기값
        this.shaking = false;            // 흔들림 효과 비활성화
    }

    /**
     * 카메라의 현재 X좌표를 반환
     * @return 현재 카메라 X좌표 (double)
     */
    public double getX() {
        return x;
    }

    /**
     * 카메라의 X좌표를 설정
     * 주로 마리오의 이동에 따라 화면을 스크롤할 때 사용
     * @param x 설정할 X좌표 값
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * 카메라의 현재 Y좌표를 반환
     * @return 현재 카메라 Y좌표 (double)
     */
    public double getY() {
        return y;
    }

    /**
     * 카메라의 Y좌표를 설정
     * 현재 게임에서는 수직 스크롤이 없으나, 확장성을 위해 유지
     * @param y 설정할 Y좌표 값
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * 화면 흔들림 효과를 활성화
     * 마리오가 적을 밟았을 때나 중요한 이벤트 발생 시 호출
     * 60프레임(1초) 동안 화면을 좌우로 흔들림
     */
    public void shakeCamera() {
        shaking = true;                 // 흔들림 효과 활성화
        frameNumber = 60;              // 60프레임 동안 지속
    }

    /**
     * 카메라를 지정된 양만큼 이동
     * 흔들림 효과가 활성화되었을 때는 좌우 흔들림을 우선적으로 적용
     *
     * @param xAmount X축 이동량 (마리오의 수평 이동 속도)
     * @param yAmount Y축 이동량 (현재는 미사용)
     */
    public void moveCam(double xAmount, double yAmount){
        // 흔들림 효과가 활성화되었고 지속 프레임이 남아있을 경우
        if(shaking && frameNumber > 0){
            // 프레임 번호에 따라 좌우 방향 결정 (짝수: 오른쪽, 홀수: 왼쪽)
            int direction = (frameNumber%2 == 0)? 1 : -1;
            // 현재 X좌표에 흔들림 효과 적용 (4픽셀씩 좌우 이동)
            x = x + 4 * direction;
            frameNumber--;              // 남은 프레임 수 감소
        }
        else{
            // 일반적인 카메라 이동 (마리오 따라가기)
            x = x + xAmount;
            y = y + yAmount;
        }

        // 흔들림 효과의 모든 프레임이 소진되었을 경우
        if(frameNumber < 0)
            shaking = false;           // 흔들림 효과 비활성화
    }
}
