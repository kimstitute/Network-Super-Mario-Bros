package manager;

// 카메라: 마리오를 따라 화면 스크롤, 흔들림 효과 제공
public class Camera {

    private double x; // 화면 왼쪽 상단의 월드 X 좌표
    private double y; // 화면 왼쪽 상단의 월드 Y 좌표
    private int frameNumber; // 흔들림 효과 남은 프레임 수
    private boolean shaking; // 화면 흔들림 활성화 여부

    public Camera(){
        this.x = 0;
        this.y = 0;
        this.frameNumber = 25;
        this.shaking = false;
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

    // 화면 흔들림 효과 시작 (60프레임 동안 좌우로 흔들림)
    public void shakeCamera() {
        shaking = true;
        frameNumber = 60;
    }

    // 카메라 이동: 흔들림 효과 중이면 우선 적용
    public void moveCam(double xAmount, double yAmount){
        if(shaking && frameNumber > 0){
            int direction = (frameNumber%2 == 0)? 1 : -1;
            x = x + 4 * direction; // 4픽셀 좌우 흔들림
            frameNumber--;
        }
        else{
            x = x + xAmount;
            y = y + yAmount;
        }

        if(frameNumber < 0)
            shaking = false;
    }
}
