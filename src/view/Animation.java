package view;

import java.awt.image.BufferedImage;

// 애니메이션: 프레임 배열을 순차적으로 전환하여 움직이는 효과 생성
public class Animation {

    private int index = 0, count = 0;
    private BufferedImage[] leftFrames, rightFrames;
    private BufferedImage currentFrame;

    public Animation(BufferedImage[] leftFrames, BufferedImage[] rightFrames){
        this.leftFrames = leftFrames;
        this.rightFrames = rightFrames;

        if (rightFrames != null && rightFrames.length > 1) {
            currentFrame = rightFrames[1];
        }
    }

    // 프레임 애니메이션 업데이트 (speed마다 다음 프레임으로 전환)
    public BufferedImage animate(int speed, boolean toRight){
        if (rightFrames == null || leftFrames == null) {
            return null;
        }
        
        count++;
        BufferedImage[] frames = toRight ? rightFrames : leftFrames;

        if(count > speed){
            nextFrame(frames);
            count = 0;
        }

        return currentFrame;
    }

    // 다음 프레임으로 전환 (순환)
    private void nextFrame(BufferedImage[] frames) {
        if(index + 3 > frames.length)
            index = 0;

        currentFrame = frames[index+2];
        index++;
    }

    public BufferedImage[] getLeftFrames() {
        return leftFrames;
    }

    public BufferedImage[] getRightFrames() {
        return rightFrames;
    }

}
