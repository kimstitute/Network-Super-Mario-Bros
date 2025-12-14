package manager;

import java.awt.*;
import java.util.ArrayList;
import model.GameObject;
import model.Map;
import model.brick.Brick;
import model.brick.OrdinaryBrick;
import model.enemy.Enemy;
import model.hero.Fireball;
import model.hero.Mario;
import model.hero.MarioForm;
import model.prize.BoostItem;
import model.prize.Coin;
import model.prize.Prize;
import network.protocol.GameStateMessage;
import view.ImageLoader;

/**
 * 게임 맵을 관리하는 클래스
 * 맵 생성, 오브젝트 관리, 충돌 감지, 게임 상태 업데이트를 담당
 *
 * @author 네트워크프로그래밍 팀
 * @version 1.0
 * @since 2024-12-14
 */
public class MapManager {
    
    /** 최대 플레이어 수 */
    public static final int MAX_PLAYERS = 4;

    /** 현재 로드된 게임 맵 객체 */
    private Map map;
    
    /** 
     * 다중 플레이어 지원을 위한 Mario 배열
     * players[0]: 사용 안 함
     * players[1~MAX_PLAYERS]: 플레이어 1~MAX_PLAYERS
     */
    private Mario[] players = new Mario[MAX_PLAYERS + 1];

    /** MapManager 생성자 */
    public MapManager() {}

    public void updateLocations() {
        if (map == null)
            return;

        map.updateLocations();
    }

    public void resetCurrentMap(GameEngine engine) {
        Mario mario = getMario();
        mario.resetLocation();
        engine.resetCamera();
        createMap(engine.getImageLoader(), map.getPath());
        map.setMario(mario);
    }

    public boolean createMap(ImageLoader loader, String path) {
        MapCreator mapCreator = new MapCreator(loader);
        map = mapCreator.createMap("/maps/" + path, 400);

        return map != null;
    }

    public void acquirePoints(int point) {
        map.getMario().acquirePoints(point);
    }

    public Mario getMario() {
        if (map != null && map.getMario() != null) {
            return map.getMario();
        }
        return players[1];
    }
    
    public Mario getPlayer(int playerId) {
        if (playerId < 1 || playerId > MAX_PLAYERS) {
            return null;
        }
        if (players[playerId] == null && map != null) {
            players[playerId] = map.getMario();
        }
        return players[playerId];
    }
    
    public Mario[] getAllPlayers() {
        return players;
    }
    
    public ArrayList<Mario> getOtherPlayers(int myPlayerId) {
        ArrayList<Mario> others = new ArrayList<>();
        for (int i = 1; i <= MAX_PLAYERS; i++) {
            if (i != myPlayerId && players[i] != null) {
                others.add(players[i]);
            }
        }
        return others;
    }

    public void fire(GameEngine engine) {
        Mario mario = getMario();
        if (mario == null) {
            return;
        }
        
        Fireball fireball = mario.fire();
        if (fireball != null) {
            map.addFireball(fireball);
            engine.playFireball();
        }
    }

    public boolean isGameOver() {
        return getMario().getRemainingLives() == 0 || map.isTimeOver();
    }

    public int getScore() {
        return getMario().getPoints();
    }

    public int getRemainingLives() {
        return getMario().getRemainingLives();
    }

    public int getCoins() {
        return getMario().getCoins();
    }

    public void drawMap(Graphics2D g2) {
        map.drawMap(g2);
    }

    public int passMission() {
        if(getMario().getX() >= map.getEndPoint().getX() && !map.getEndPoint().isTouched()){
            map.getEndPoint().setTouched(true);
            int height = (int)getMario().getY();
            return height * 2;
        }
        else
            return -1;
    }

    public boolean endLevel(){
        return getMario().getX() >= map.getEndPoint().getX() + 320;
    }

    public void checkCollisions(GameEngine engine) {
        if (map == null) {
            return;
        }

        checkBottomCollisions(engine);
        checkTopCollisions(engine);
        checkMarioHorizontalCollision(engine);
        checkEnemyCollisions();
        checkPrizeCollision();
        checkPrizeContact(engine);
        checkFireballContact();
    }

    private void checkBottomCollisions(GameEngine engine) {
        Mario mario = getMario();
        ArrayList<Brick> bricks = map.getAllBricks();
        ArrayList<Enemy> enemies = map.getEnemies();
        ArrayList<GameObject> toBeRemoved = new ArrayList<>();

        Rectangle marioBottomBounds = mario.getBottomBounds();

        if (!mario.isJumping())
            mario.setFalling(true);

        for (Brick brick : bricks) {
            Rectangle brickTopBounds = brick.getTopBounds();
            if (marioBottomBounds.intersects(brickTopBounds)) {
                mario.setY(brick.getY() - mario.getDimension().height + 1);
                mario.setFalling(false);
                mario.setVelY(0);
            }
        }

        for (Enemy enemy : enemies) {
            Rectangle enemyTopBounds = enemy.getTopBounds();
            if (marioBottomBounds.intersects(enemyTopBounds)) {
                mario.acquirePoints(100);
                toBeRemoved.add(enemy);
                engine.playStomp();
            }
        }

        if (mario.getY() + mario.getDimension().height >= map.getBottomBorder()) {
            mario.setY(map.getBottomBorder() - mario.getDimension().height);
            mario.setFalling(false);
            mario.setVelY(0);
        }

        removeObjects(toBeRemoved);
    }

    private void checkTopCollisions(GameEngine engine) {
        Mario mario = getMario();
        ArrayList<Brick> bricks = map.getAllBricks();

        Rectangle marioTopBounds = mario.getTopBounds();
        for (Brick brick : bricks) {
            Rectangle brickBottomBounds = brick.getBottomBounds();
            if (marioTopBounds.intersects(brickBottomBounds)) {
                mario.setVelY(0);
                mario.setY(brick.getY() + brick.getDimension().height);
                Prize prize = brick.reveal(engine);
                if(prize != null)
                    map.addRevealedPrize(prize);
            }
        }
    }

    private void checkMarioHorizontalCollision(GameEngine engine){
        Mario mario = getMario();
        ArrayList<Brick> bricks = map.getAllBricks();
        ArrayList<Enemy> enemies = map.getEnemies();
        ArrayList<GameObject> toBeRemoved = new ArrayList<>();

        boolean marioDies = false;
        boolean toRight = mario.getToRight();

        Rectangle marioBounds = toRight ? mario.getRightBounds() : mario.getLeftBounds();

        for (Brick brick : bricks) {
            Rectangle brickBounds = !toRight ? brick.getRightBounds() : brick.getLeftBounds();
            if (marioBounds.intersects(brickBounds)) {
                mario.setVelX(0);
                if(toRight)
                    mario.setX(brick.getX() - mario.getDimension().width);
                else
                    mario.setX(brick.getX() + brick.getDimension().width);
            }
        }

        for(Enemy enemy : enemies){
            Rectangle enemyBounds = !toRight ? enemy.getRightBounds() : enemy.getLeftBounds();
            if (marioBounds.intersects(enemyBounds)) {
                marioDies = mario.onTouchEnemy(engine);
                toBeRemoved.add(enemy);
            }
        }
        removeObjects(toBeRemoved);


        if (mario.getX() <= engine.getCameraLocation().getX() && mario.getVelX() < 0) {
            mario.setVelX(0);
            mario.setX(engine.getCameraLocation().getX());
        }

        if(marioDies) {
            resetCurrentMap(engine);
        }
    }

    private void checkEnemyCollisions() {
        ArrayList<Brick> bricks = map.getAllBricks();
        ArrayList<Enemy> enemies = map.getEnemies();

        for (Enemy enemy : enemies) {
            boolean standsOnBrick = false;

            for (Brick brick : bricks) {
                Rectangle enemyBounds = enemy.getLeftBounds();
                Rectangle brickBounds = brick.getRightBounds();

                Rectangle enemyBottomBounds = enemy.getBottomBounds();
                Rectangle brickTopBounds = brick.getTopBounds();

                if (enemy.getVelX() > 0) {
                    enemyBounds = enemy.getRightBounds();
                    brickBounds = brick.getLeftBounds();
                }

                if (enemyBounds.intersects(brickBounds)) {
                    enemy.setVelX(-enemy.getVelX());
                }

                if (enemyBottomBounds.intersects(brickTopBounds)){
                    enemy.setFalling(false);
                    enemy.setVelY(0);
                    enemy.setY(brick.getY()-enemy.getDimension().height);
                    standsOnBrick = true;
                }
            }

            if(enemy.getY() + enemy.getDimension().height > map.getBottomBorder()){
                enemy.setFalling(false);
                enemy.setVelY(0);
                enemy.setY(map.getBottomBorder()-enemy.getDimension().height);
            }

            if (!standsOnBrick && enemy.getY() < map.getBottomBorder()){
                enemy.setFalling(true);
            }
        }
    }

    private void checkPrizeCollision() {
        ArrayList<Prize> prizes = map.getRevealedPrizes();
        ArrayList<Brick> bricks = map.getAllBricks();

        for (Prize prize : prizes) {
            if (prize instanceof BoostItem) {
                BoostItem boost = (BoostItem) prize;
                Rectangle prizeBottomBounds = boost.getBottomBounds();
                Rectangle prizeRightBounds = boost.getRightBounds();
                Rectangle prizeLeftBounds = boost.getLeftBounds();
                boost.setFalling(true);

                for (Brick brick : bricks) {
                    Rectangle brickBounds;

                    if (boost.isFalling()) {
                        brickBounds = brick.getTopBounds();

                        if (brickBounds.intersects(prizeBottomBounds)) {
                            boost.setFalling(false);
                            boost.setVelY(0);
                            boost.setY(brick.getY() - boost.getDimension().height + 1);
                            if (boost.getVelX() == 0)
                                boost.setVelX(2);
                        }
                    }

                    if (boost.getVelX() > 0) {
                        brickBounds = brick.getLeftBounds();

                        if (brickBounds.intersects(prizeRightBounds)) {
                            boost.setVelX(-boost.getVelX());
                        }
                    } else if (boost.getVelX() < 0) {
                        brickBounds = brick.getRightBounds();

                        if (brickBounds.intersects(prizeLeftBounds)) {
                            boost.setVelX(-boost.getVelX());
                        }
                    }
                }

                if (boost.getY() + boost.getDimension().height > map.getBottomBorder()) {
                    boost.setFalling(false);
                    boost.setVelY(0);
                    boost.setY(map.getBottomBorder() - boost.getDimension().height);
                    if (boost.getVelX() == 0)
                        boost.setVelX(2);
                }

            }
        }
    }

    private void checkPrizeContact(GameEngine engine) {
        ArrayList<Prize> prizes = map.getRevealedPrizes();
        ArrayList<GameObject> toBeRemoved = new ArrayList<>();

        Rectangle marioBounds = getMario().getBounds();
        for(Prize prize : prizes){
            Rectangle prizeBounds = prize.getBounds();
            if (prizeBounds.intersects(marioBounds)) {
                prize.onTouch(getMario(), engine);
                toBeRemoved.add((GameObject) prize);
            } else if(prize instanceof Coin){
                prize.onTouch(getMario(), engine);
            }
        }

        removeObjects(toBeRemoved);
    }

    private void checkFireballContact() {
        ArrayList<Fireball> fireballs = map.getFireballs();
        ArrayList<Enemy> enemies = map.getEnemies();
        ArrayList<Brick> bricks = map.getAllBricks();
        ArrayList<GameObject> toBeRemoved = new ArrayList<>();

        for(Fireball fireball : fireballs){
            Rectangle fireballBounds = fireball.getBounds();

            for(Enemy enemy : enemies){
                Rectangle enemyBounds = enemy.getBounds();
                if (fireballBounds.intersects(enemyBounds)) {
                    acquirePoints(100);
                    toBeRemoved.add(enemy);
                    toBeRemoved.add(fireball);
                }
            }

            for(Brick brick : bricks){
                Rectangle brickBounds = brick.getBounds();
                if (fireballBounds.intersects(brickBounds)) {
                    toBeRemoved.add(fireball);
                }
            }
        }

        removeObjects(toBeRemoved);
    }

    private void removeObjects(ArrayList<GameObject> list){
        if(list == null)
            return;

        for(GameObject object : list){
            if(object instanceof Fireball){
                map.removeFireball((Fireball)object);
            }
            else if(object instanceof Enemy){
                map.removeEnemy((Enemy)object);
            }
            else if(object instanceof Coin || object instanceof BoostItem){
                map.removePrize((Prize)object);
            }
        }
    }

    public void addRevealedBrick(OrdinaryBrick ordinaryBrick) {
        map.addRevealedBrick(ordinaryBrick);
    }

    public void updateTime(){
        if(map != null)
            map.updateTime(1);
    }

    public int getRemainingTime() {
        return (int)map.getRemainingTime();
    }
    
    public GameStateMessage collectGameState(Camera camera) {
        if (map == null) {
            return null;
        }
        
        GameStateMessage.PlayerState[] playerStates = new GameStateMessage.PlayerState[MAX_PLAYERS + 1];
        for (int i = 1; i <= MAX_PLAYERS; i++) {
            playerStates[i] = createPlayerState(getPlayer(i));
        }
        
        ArrayList<Enemy> enemies = map.getEnemies();
        GameStateMessage.EnemyState[] enemyStates = new GameStateMessage.EnemyState[enemies.size()];
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            GameStateMessage.EnemyState es = new GameStateMessage.EnemyState();
            es.x = (int) enemy.getX();
            es.y = (int) enemy.getY();
            es.alive = true;
            es.type = enemy.getClass().getSimpleName();
            es.direction = enemy.getVelX() > 0;
            enemyStates[i] = es;
        }
        
        ArrayList<Prize> prizes = map.getRevealedPrizes();
        GameStateMessage.ItemState[] itemStates = new GameStateMessage.ItemState[prizes.size()];
        for (int i = 0; i < prizes.size(); i++) {
            Prize prize = prizes.get(i);
            GameStateMessage.ItemState is = new GameStateMessage.ItemState();
            is.x = (int) ((GameObject) prize).getX();
            is.y = (int) ((GameObject) prize).getY();
            is.collected = false;
            is.type = prize.getClass().getSimpleName();
            itemStates[i] = is;
        }
        
        ArrayList<Fireball> fireballs = map.getFireballs();
        GameStateMessage.FireballState[] fireballStates = new GameStateMessage.FireballState[fireballs.size()];
        for (int i = 0; i < fireballs.size(); i++) {
            Fireball fireball = fireballs.get(i);
            GameStateMessage.FireballState fs = new GameStateMessage.FireballState();
            fs.x = (int) fireball.getX();
            fs.y = (int) fireball.getY();
            fs.active = true;
            fs.direction = fireball.getVelX() > 0;
            fireballStates[i] = fs;
        }
        
        GameStateMessage.GameInfo gameInfo = new GameStateMessage.GameInfo();
        gameInfo.remainingTime = getRemainingTime();
        gameInfo.cameraX = camera != null ? camera.getX() : 0.0;
        gameInfo.mapName = map.getPath();
        
        return new GameStateMessage(playerStates, enemyStates, itemStates, fireballStates, gameInfo);
    }
    
    public void processInput(int playerId, int keyCode, boolean pressed, GameEngine engine) {
        Mario mario = getPlayer(playerId);
        if (mario == null) {
            return;
        }
        
        if (pressed) {
            switch (keyCode) {
                case 38:
                case 87:
                    mario.jump(engine);
                    break;
                case 39:
                case 68:
                    mario.move(true, null);
                    break;
                case 37:
                case 65:
                    mario.move(false, null);
                    break;
                case 32:
                    fire(engine);
                    break;
            }
        } else {
            mario.setVelX(0);
        }
    }
    
    public void updateCamera(Camera camera) {
        if (camera == null || map == null) {
            return;
        }
        
        Mario mario = getMario();
        if (mario != null) {
            double marioVelocityX = mario.getVelX();
            double shiftAmount = 0;
            
            if (marioVelocityX > 0 && mario.getX() - 600 > camera.getX()) {
                shiftAmount = marioVelocityX;
            }
            
            camera.moveCam(shiftAmount, 0);
        }
    }
    
    private GameStateMessage.PlayerState createPlayerState(Mario mario) {
        if (mario == null) {
            return null;
        }
        
        GameStateMessage.PlayerState ps = new GameStateMessage.PlayerState();
        ps.x = (int) mario.getX();
        ps.y = (int) mario.getY();
        ps.velX = (int) mario.getVelX();
        ps.velY = (int) mario.getVelY();
        ps.jumping = mario.isJumping();
        ps.toRight = mario.getToRight();
        ps.lives = mario.getRemainingLives();
        ps.coins = mario.getCoins();
        ps.points = mario.getPoints();
        
        MarioForm marioForm = mario.getMarioForm();
        if (marioForm.isFire()) {
            ps.form = "Fire";
        } else if (marioForm.isSuper()) {
            ps.form = "Super";
        } else {
            ps.form = "Small";
        }
        
        return ps;
    }
    
    public void applyGameState(GameStateMessage state) {
        if (state == null || map == null) {
            return;
        }
        
        GameStateMessage.PlayerState[] playerStates = state.getPlayers();
        if (playerStates != null) {
            for (int i = 1; i <= MAX_PLAYERS && i < playerStates.length; i++) {
                applyPlayerState(getPlayer(i), playerStates[i]);
            }
        }
    }
    
    private void applyPlayerState(Mario mario, GameStateMessage.PlayerState state) {
        if (mario == null || state == null) {
            return;
        }
        
        mario.setX(state.x);
        mario.setY(state.y);
        mario.setVelX(state.velX);
        mario.setVelY(state.velY);
        mario.setJumping(state.jumping);
    }
}
