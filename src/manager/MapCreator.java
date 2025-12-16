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

// 맵 생성자: PNG 이미지의 픽셀 색상을 분석하여 게임 오브젝트 생성
class MapCreator {

    private ImageLoader imageLoader;
    private BufferedImage backgroundImage;
    private BufferedImage superMushroom, oneUpMushroom, fireFlower, coin;
    private BufferedImage ordinaryBrick, surpriseBrick, groundBrick, pipe;
    private BufferedImage goombaLeft, goombaRight, koopaLeft, koopaRight, endFlag;

    // 스프라이트 시트에서 모든 게임 요소의 이미지 추출
    MapCreator(ImageLoader imageLoader) {

        this.imageLoader = imageLoader;
        BufferedImage sprite = imageLoader.loadImage("/sprite.png");
        this.backgroundImage = imageLoader.loadImage("/background.png");
        
        this.superMushroom = imageLoader.getSubImage(sprite, 2, 5, 48, 48);
        this.oneUpMushroom= imageLoader.getSubImage(sprite, 3, 5, 48, 48);
        this.fireFlower= imageLoader.getSubImage(sprite, 4, 5, 48, 48);
        this.coin = imageLoader.getSubImage(sprite, 1, 5, 48, 48);
        
        this.ordinaryBrick = imageLoader.getSubImage(sprite, 1, 1, 48, 48);
        this.surpriseBrick = imageLoader.getSubImage(sprite, 2, 1, 48, 48);
        this.groundBrick = imageLoader.getSubImage(sprite, 2, 2, 48, 48);
        this.pipe = imageLoader.getSubImage(sprite, 3, 1, 96, 96);
        
        this.goombaLeft = imageLoader.getSubImage(sprite, 2, 4, 48, 48);
        this.goombaRight = imageLoader.getSubImage(sprite, 5, 4, 48, 48);
        this.koopaLeft = imageLoader.getSubImage(sprite, 1, 3, 48, 64);
        this.koopaRight = imageLoader.getSubImage(sprite, 4, 3, 48, 64);
        this.endFlag = imageLoader.getSubImage(sprite, 5, 1, 48, 48);

    }

    // PNG 이미지의 각 픽셀 색상에 따라 게임 오브젝트 생성
    // 색상 매핑: 회색=마리오, 파란색=일반블록, 노란색계열=물음표블록(타입별), 빨간색=지면, 초록=파이프, 하늘색=굼바, 분홍=쿠파, 보라=깃발
    Map createMap(String mapPath, double timeLimit) {
        if (imageLoader == null) {
            System.out.println("[MAP] Headless mode: Cannot load map from image");
            return null;
        }
        
        BufferedImage mapImage = imageLoader.loadImage(mapPath);

        if (mapImage == null) {
            System.out.println("Given path is invalid...");
            return null;
        }

        Map createdMap = new Map(timeLimit, backgroundImage);
        String[] paths = mapPath.split("/");
        createdMap.setPath(paths[paths.length-1]);

        int pixelMultiplier = 48; // 1픽셀 = 48 게임 단위

        // 기본 오브젝트
        int mario = new Color(160, 160, 160).getRGB();
        int ordinaryBrick = new Color(0, 0, 255).getRGB();
        int groundBrick = new Color(255, 0, 0).getRGB();
        int pipe = new Color(0, 255, 0).getRGB();
        int goomba = new Color(0, 255, 255).getRGB();
        int koopa = new Color(255, 0, 255).getRGB();
        int end = new Color(160, 0, 160).getRGB();
        
        // 배경 색상 (무시)
        int backgroundColor = new Color(0, 0, 0).getRGB();        // 검은색 - 빈 공간
        int decorativeColor = new Color(127, 51, 0).getRGB();     // 갈색 - 배경 장식
        
        // 물음표 블록 (아이템 타입별)
        int surpriseBrick_random = new Color(255, 255, 0).getRGB();     // 밝은 노란색 - 랜덤 (기존)
        int surpriseBrick_coin = new Color(255, 200, 0).getRGB();       // 주황빛 노란색 - 코인
        int surpriseBrick_mushroom = new Color(255, 100, 0).getRGB();   // 짙은 주황색 - 슈퍼 버섯
        int surpriseBrick_fireFlower = new Color(255, 150, 0).getRGB(); // 중간 주황색 - 파이어 플라워
        int surpriseBrick_1up = new Color(200, 255, 0).getRGB();        // 연두색 - 1UP 버섯
        
        // 숨겨진 아이템 블록 (일반 블록처럼 보임)
        int hiddenBrick_coin = new Color(0, 0, 254).getRGB();           // 파란색에서 1픽셀 차이 - 코인

        for (int x = 0; x < mapImage.getWidth(); x++) {
            for (int y = 0; y < mapImage.getHeight(); y++) {

                int currentPixel = mapImage.getRGB(x, y);
                int xLocation = x*pixelMultiplier;
                int yLocation = y*pixelMultiplier;

                // 배경 색상은 무시
                if (currentPixel == backgroundColor || currentPixel == decorativeColor) {
                    continue;
                }
                
                if (currentPixel == ordinaryBrick) {
                    Brick brick = new OrdinaryBrick(xLocation, yLocation, this.ordinaryBrick);
                    createdMap.addBrick(brick);
                }
                
                // 물음표 블록 - 랜덤 아이템
                else if (currentPixel == surpriseBrick_random) {
                    Prize prize = generateRandomPrize(xLocation, yLocation);
                    Brick brick = new SurpriseBrick(xLocation, yLocation, this.surpriseBrick, prize);
                    createdMap.addBrick(brick);
                }
                // 물음표 블록 - 코인
                else if (currentPixel == surpriseBrick_coin) {
                    Prize prize = new Coin(xLocation, yLocation, this.coin, 50);
                    Brick brick = new SurpriseBrick(xLocation, yLocation, this.surpriseBrick, prize);
                    createdMap.addBrick(brick);
                }
                // 물음표 블록 - 슈퍼 버섯
                else if (currentPixel == surpriseBrick_mushroom) {
                    Prize prize = new SuperMushroom(xLocation, yLocation, this.superMushroom);
                    Brick brick = new SurpriseBrick(xLocation, yLocation, this.surpriseBrick, prize);
                    createdMap.addBrick(brick);
                }
                // 물음표 블록 - 파이어 플라워
                else if (currentPixel == surpriseBrick_fireFlower) {
                    Prize prize = new FireFlower(xLocation, yLocation, this.fireFlower);
                    Brick brick = new SurpriseBrick(xLocation, yLocation, this.surpriseBrick, prize);
                    createdMap.addBrick(brick);
                }
                // 물음표 블록 - 1UP 버섯
                else if (currentPixel == surpriseBrick_1up) {
                    Prize prize = new OneUpMushroom(xLocation, yLocation, this.oneUpMushroom);
                    Brick brick = new SurpriseBrick(xLocation, yLocation, this.surpriseBrick, prize);
                    createdMap.addBrick(brick);
                }
                // 숨겨진 블록 - 코인 (일반 블록처럼 보임)
                else if (currentPixel == hiddenBrick_coin) {
                    Prize prize = new Coin(xLocation, yLocation, this.coin, 100);
                    Brick brick = new SurpriseBrick(xLocation, yLocation, this.ordinaryBrick, prize);
                    brick.setEmpty(false);
                    createdMap.addBrick(brick);
                }
                
                else if (currentPixel == pipe) {
                    Brick brick = new Pipe(xLocation, yLocation, this.pipe);
                    createdMap.addGroundBrick(brick);
                }
                else if (currentPixel == groundBrick) {
                    Brick brick = new GroundBrick(xLocation, yLocation, this.groundBrick);
                    createdMap.addGroundBrick(brick);
                }
                else if (currentPixel == goomba) {
                    Enemy enemy = new Goomba(xLocation, yLocation, this.goombaLeft);
                    ((Goomba)enemy).setRightImage(goombaRight);
                    createdMap.addEnemy(enemy);
                }
                else if (currentPixel == koopa) {
                    Enemy enemy = new KoopaTroopa(xLocation, yLocation, this.koopaLeft);
                    ((KoopaTroopa)enemy).setRightImage(koopaRight);
                    createdMap.addEnemy(enemy);
                }
                else if (currentPixel == mario) {
                    Mario marioObject = new Mario(xLocation, yLocation, imageLoader);
                    createdMap.setMario(marioObject);
                }
                else if(currentPixel == end){
                    EndFlag endPoint= new EndFlag(xLocation+24, yLocation, endFlag);
                    createdMap.setEndPoint(endPoint);
                }
            }
        }

        System.out.println("Map is created..");
        return createdMap;
    }

    // 물음표 블록 아이템 랜덤 생성 (8.3% 슈퍼버섯, 8.3% 파이어플라워, 8.3% 1업버섯, 75% 코인)
    // 참고: 랜덤 블록은 레거시 지원용, 신규 맵은 아이템 타입별 블록 사용 권장
    private Prize generateRandomPrize(double x, double y){
        Prize generated;
        int random = (int)(Math.random() * 12);

        if(random == 0){
            generated = new SuperMushroom(x, y, this.superMushroom);
        }
        else if(random == 1){
            generated = new FireFlower(x, y, this.fireFlower);
        }
        else if(random == 2){
            generated = new OneUpMushroom(x, y, this.oneUpMushroom);
        }
        else{
            generated = new Coin(x, y, this.coin, 50);
        }

        return generated;
    }

}
