package view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

// 이미지 로더: 게임 리소스 이미지 파일 로드 및 스프라이트 추출
public class ImageLoader {

    private BufferedImage marioForms;
    private BufferedImage brickAnimation;

    public ImageLoader(){
        marioForms = loadImage("/mario-forms.png");
        brickAnimation = loadImage("/brick-animation.png");
    }

    // src/media/ 경로에서 이미지 로드
    public BufferedImage loadImage(String path){
        BufferedImage imageToReturn = null;

        try {
            File file = new File("src/media" + path);
            if (file.exists()) {
                imageToReturn = ImageIO.read(file);
                System.out.println("[ImageLoader] Loaded image from file: " + file.getAbsolutePath());
            } else {
                System.err.println("[ImageLoader] File not found: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("[ImageLoader] Error loading image: " + path);
            e.printStackTrace();
        }

        return imageToReturn;
    }

    public BufferedImage loadImage(File file){
        BufferedImage imageToReturn = null;

        try {
            imageToReturn = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageToReturn;
    }

    // 스프라이트 시트에서 특정 영역 추출 (col, row 1부터 시작)
    public BufferedImage getSubImage(BufferedImage image, int col, int row, int w, int h){
        if (image == null) {
            return null;
        }
        if((col == 1 || col == 4) && row == 3){ // 쿠파는 Y 오프셋 특수 처리
            return image.getSubimage((col-1)*48, 128, w, h);
        }
        return image.getSubimage((col-1)*48, (row-1)*48, w, h);
    }

    // 마리오 왼쪽 방향 프레임 배열 추출 (marioForm: 0=Small, 1=Super, 2=Fire)
    public BufferedImage[] getLeftFrames(int marioForm){
        if (marioForms == null) return null;
        
        BufferedImage[] leftFrames = new BufferedImage[5];
        int col = 1;
        int width = 52, height = 48;

        if(marioForm == 1) {
            col = 4;
            width = 48;
            height = 96;
        }
        else if(marioForm == 2){
            col = 7;
            width = 48;
            height = 96;
        }

        for(int i = 0; i < 5; i++){
            leftFrames[i] = marioForms.getSubimage((col-1)*width, (i)*height, width, height);
        }
        return leftFrames;
    }

    // 마리오 오른쪽 방향 프레임 배열 추출
    public BufferedImage[] getRightFrames(int marioForm){
        if (marioForms == null) return null;
        
        BufferedImage[] rightFrames = new BufferedImage[5];
        int col = 2;
        int width = 52, height = 48;

        if(marioForm == 1) {
            col = 5;
            width = 48;
            height = 96;
        }
        else if(marioForm == 2){
            col = 8;
            width = 48;
            height = 96;
        }

        for(int i = 0; i < 5; i++){
            rightFrames[i] = marioForms.getSubimage((col-1)*width, (i)*height, width, height);
        }
        return rightFrames;
    }

    // 블록 부서지는 애니메이션 프레임 배열 추출
    public BufferedImage[] getBrickFrames() {
        BufferedImage[] frames = new BufferedImage[4];
        for(int i = 0; i < 4; i++){
            frames[i] = brickAnimation.getSubimage(i*105, 0, 105, 105);
        }
        return frames;
    }

}
