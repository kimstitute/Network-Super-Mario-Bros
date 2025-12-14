package manager;

import java.awt.*;
import java.awt.image.BufferedImage;
import model.EndFlag;
import model.Map;
import model.brick.*;
import model.enemy.Enemy;
import model.enemy.Goomba;
import model.enemy.KoopaTroopa;
import model.hero.Mario;
import model.prize.*;
import view.ImageLoader;

/**
 * 이미지 기반 맵 데이터를 생성하는 맵 생성자 클래스
 * PNG 이미지 파일의 픽셀 색상을 분석하여 게임 오브젝트를 생성
 * 각 색상은 특정 게임 요소(블록, 적, 아이템 등)에 매핑됨
 *
 * @author 네트워크프로그래밍 팀
 * @version 1.0
 * @since 2024-12-14
 */
class MapCreator {

    /**
     * 이미지 로더 참조 - 스프라이트 이미지와 배경 이미지를 로드하는 데 사용
     */
    private ImageLoader imageLoader;

    /**
     * 게임 배경 이미지 - 맵의 전체 배경을 표시하는 데 사용
     */
    private BufferedImage backgroundImage;
    
    /**
     * 아이템 이미지들 - 각각의 아이템 종류에 대한 스프라이트 이미지
     */
    private BufferedImage superMushroom, oneUpMushroom, fireFlower, coin;
    
    /**
     * 블록 이미지들 - 각각의 블록 종류에 대한 스프라이트 이미지
     */
    private BufferedImage ordinaryBrick, surpriseBrick, groundBrick, pipe;
    
    /**
     * 적 이미지들 - 각각의 적 종류와 방향에 대한 스프라이트 이미지
     */
    private BufferedImage goombaLeft, goombaRight, koopaLeft, koopaRight, endFlag;


    /**
     * 맵 생성자 생성자
     * 스프라이트 시트에서 모든 게임 요소의 이미지를 추출하여 초기화
     *
     * @param imageLoader 이미지 로드를 담당할 ImageLoader 인스턴스
     */
    MapCreator(ImageLoader imageLoader) {

        this.imageLoader = imageLoader;
        // 스프라이트 시트 로드 - 모든 게임 요소의 이미지가 포함됨
        BufferedImage sprite = imageLoader.loadImage("/sprite.png");

        // 배경 이미지 로드
        this.backgroundImage = imageLoader.loadImage("/background.png");
        
        // 아이템 이미지 추출 (48x48 픽셀)
        this.superMushroom = imageLoader.getSubImage(sprite, 2, 5, 48, 48);    // 슈퍼 버섯
        this.oneUpMushroom= imageLoader.getSubImage(sprite, 3, 5, 48, 48);     // 1업 버섯
        this.fireFlower= imageLoader.getSubImage(sprite, 4, 5, 48, 48);        // 파이어플라워
        this.coin = imageLoader.getSubImage(sprite, 1, 5, 48, 48);             // 코인
        
        // 블록 이미지 추출
        this.ordinaryBrick = imageLoader.getSubImage(sprite, 1, 1, 48, 48);     // 일반 블록
        this.surpriseBrick = imageLoader.getSubImage(sprite, 2, 1, 48, 48);    // 서프라이즈 블록
        this.groundBrick = imageLoader.getSubImage(sprite, 2, 2, 48, 48);       // 지면 블록
        this.pipe = imageLoader.getSubImage(sprite, 3, 1, 96, 96);            // 파이프 (96x96)
        
        // 적 이미지 추출
        this.goombaLeft = imageLoader.getSubImage(sprite, 2, 4, 48, 48);       // 굼바 왼쪽
        this.goombaRight = imageLoader.getSubImage(sprite, 5, 4, 48, 48);      // 굼바 오른쪽
        this.koopaLeft = imageLoader.getSubImage(sprite, 1, 3, 48, 64);       // 쿠파 왼쪽 (48x64)
        this.koopaRight = imageLoader.getSubImage(sprite, 4, 3, 48, 64);      // 쿠파 오른쪽
        this.endFlag = imageLoader.getSubImage(sprite, 5, 1, 48, 48);          // 종료 깃발

    }
/**
 * 이미지 파일로부터 게임 맵을 생성하는 핵심 메서드
 * PNG 이미지의 각 픽셀 색상을 분석하여 해당하는 게임 오브젝트를 생성
 * 색상-오브젝트 매핑을 통해 시각적 맵 디자인을 게임 데이터로 변환
 *
 * @param mapPath 생성할 맵 이미지의 경로
 * @param timeLimit 맵의 시간 제한 (초 단위)
 * @return 생성된 Map 객체, 실패 시 null 반환
 */
Map createMap(String mapPath, double timeLimit) {
    // 지정된 경로에서 맵 이미지 로드
    BufferedImage mapImage = imageLoader.loadImage(mapPath);

    // 이미지 로드 실패 시 에러 메시지 출력 후 null 반환
    if (mapImage == null) {
        System.out.println("Given path is invalid...");
        return null;
    }

    // 새로운 맵 객체 생성 (시간 제한과 배경 이미지 설정)
    Map createdMap = new Map(timeLimit, backgroundImage);
    // 경로에서 파일 이름만 추출하여 맵에 설정
    String[] paths = mapPath.split("/");
    createdMap.setPath(paths[paths.length-1]);

    // 픽셀을 게임 좌표로 변환하는 배수 (48픽셀 = 1 게임 단위)
    int pixelMultiplier = 48;

    // 각 게임 요소에 해당하는 색상 값 정의 (RGB)
    int mario = new Color(160, 160, 160).getRGB();           // 마리오 시작 위치 (회색)
    int ordinaryBrick = new Color(0, 0, 255).getRGB();       // 일반 블록 (파란색)
    int surpriseBrick = new Color(255, 255, 0).getRGB();      // 서프라이즈 블록 (노란색)
    int groundBrick = new Color(255, 0, 0).getRGB();         // 지면 블록 (빨간색)
    int pipe = new Color(0, 255, 0).getRGB();               // 파이프 (초록색)
    int goomba = new Color(0, 255, 255).getRGB();           // 굼바 (하늘색)
    int koopa = new Color(255, 0, 255).getRGB();            // 쿠파 트루파 (분홍색)
    int end = new Color(160, 0, 160).getRGB();              // 종료 깃발 (보라색)

    // 이미지의 모든 픽셀을 순회하며 게임 오브젝트 생성
    for (int x = 0; x < mapImage.getWidth(); x++) {
        for (int y = 0; y < mapImage.getHeight(); y++) {

            int currentPixel = mapImage.getRGB(x, y);           // 현재 픽셀의 색상
            int xLocation = x*pixelMultiplier;                  // 게임 월드 X좌표
            int yLocation = y*pixelMultiplier;                  // 게임 월드 Y좌표

            // 픽셀 색상에 따라 해당하는 게임 오브젝트 생성
            if (currentPixel == ordinaryBrick) {
                // 일반 블록 생성
                Brick brick = new OrdinaryBrick(xLocation, yLocation, this.ordinaryBrick);
                createdMap.addBrick(brick);
            }

            else if (currentPixel == surpriseBrick) {
                // 서프라이즈 블록 생성 - 내부에 랜덤 아이템 포함
                Prize prize = generateRandomPrize(xLocation, yLocation);
                Brick brick = new SurpriseBrick(xLocation, yLocation, this.surpriseBrick, prize);
                createdMap.addBrick(brick);
            }
            else if (currentPixel == pipe) {
                // 파이프 생성 (지면 블록으로 취급)
                Brick brick = new Pipe(xLocation, yLocation, this.pipe);
                createdMap.addGroundBrick(brick);
            }
            else if (currentPixel == groundBrick) {
                // 지면 블록 생성
                Brick brick = new GroundBrick(xLocation, yLocation, this.groundBrick);
                createdMap.addGroundBrick(brick);
            }
            else if (currentPixel == goomba) {
                // 굼바 적 생성 - 왼쪽 이미지 설정, 오른쪽 이미지도 추가
                Enemy enemy = new Goomba(xLocation, yLocation, this.goombaLeft);
                ((Goomba)enemy).setRightImage(goombaRight);
                createdMap.addEnemy(enemy);
            }
            else if (currentPixel == koopa) {
                // 쿠파 트루파 적 생성 - 왼쪽 이미지 설정, 오른쪽 이미지도 추가
                Enemy enemy = new KoopaTroopa(xLocation, yLocation, this.koopaLeft);
                ((KoopaTroopa)enemy).setRightImage(koopaRight);
                createdMap.addEnemy(enemy);
            }
            else if (currentPixel == mario) {
                // 마리오 시작 위치 설정
                Mario marioObject = new Mario(xLocation, yLocation);
                createdMap.setMario(marioObject);
            }
            else if(currentPixel == end){
                // 종료 깃발 설정 (X좌표에 24픽셀 오프셋 추가)
                EndFlag endPoint= new EndFlag(xLocation+24, yLocation, endFlag);
                createdMap.setEndPoint(endPoint);
            }
        }
    }

    System.out.println("Map is created..");  // 맵 생성 완료 메시지
    return createdMap;                     // 생성된 맵 객체 반환
}

    /**
     * 서프라이즈 블록에서 나올 랜덤 아이템을 생성하는 메서드
     * 12개의 확률 중 3개는 특수 아이템, 9개는 코인
     *
     * @param x 아이템이 생성될 X좌표
     * @param y 아이템이 생성될 Y좌표
     * @return 생성된 Prize 객체
     */
    private Prize generateRandomPrize(double x, double y){
        Prize generated;
        int random = (int)(Math.random() * 12);  // 0-11 사이의 랜덤 수

        // 랜덤 수에 따라 다른 아이템 생성 (각 1/12 확률)
        if(random == 0){ //super mushroom - 슈퍼 버섯 (25% 확률)
            generated = new SuperMushroom(x, y, this.superMushroom);
        }
        else if(random == 1){ //fire flower - 파이어플라워 (25% 확률)
            generated = new FireFlower(x, y, this.fireFlower);
        }
        else if(random == 2){ //one up mushroom - 1업 버섯 (25% 확률)
            generated = new OneUpMushroom(x, y, this.oneUpMushroom);
        }
        else{ //coin - 코인 (75% 확률, 9/12 경우)
            generated = new Coin(x, y, this.coin, 50);
        }

        return generated;  // 생성된 아이템 반환
    }


}
