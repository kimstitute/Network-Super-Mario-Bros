package model;

import java.awt.image.BufferedImage;

// 종료 깃발: 마리오가 터치하면 하강 애니메이션 실행
public class EndFlag extends GameObject{

    private boolean touched = false;

    public EndFlag(double x, double y, BufferedImage style) {
        super(x, y, style);
    }

    // 터치되면 깃발이 아래로 떨어지는 애니메이션
    @Override
    public void updateLocation() {
        if(touched){
            if(getY() + getDimension().getHeight() >= 576){
                setFalling(false);
                setVelY(0);
                setY(576 - getDimension().getHeight());
            }
            super.updateLocation();
        }
    }

    public boolean isTouched() {
        return touched;
    }

    public void setTouched(boolean touched) {
        this.touched = touched;
    }
}
