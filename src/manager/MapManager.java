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

// 맵 관리자: 맵 생성, 충돌 감지, 플레이어 관리, 게임 상태 직렬화
public class MapManager {
    
    public static final int MAX_PLAYERS = 4;

    private Map map;
    private Mario[] players = new Mario[MAX_PLAYERS + 1]; // players[0]=사용 안함, players[1~4]=플레이어
    private ImageLoader imageLoader;

    public MapManager() {}

    // 모든 플레이어와 맵 오브젝트의 위치 업데이트
    public void updateLocations() {
        if (map == null)
            return;

        for (int i = 1; i <= MAX_PLAYERS; i++) {
            if (players[i] != null) {
                players[i].updateLocation();
                players[i].updateDamageInvincibility(0.016); // 60Hz 기준
            }
        }
        
        map.updateLocations();
    }

    // 플레이어 리스폰 (생명이 0이면 클라이언트가 게임오버 처리)
    public void respawnPlayer(Mario mario) {
        System.out.println("[MapManager] respawnPlayer called - Lives: " + mario.getRemainingLives());
        
        if (mario.getRemainingLives() <= 0) {
            System.out.println("[MapManager] Player out of lives - will be handled by client");
            return;
        }
        
        System.out.println("[MapManager] Respawning player at starting position");
        mario.resetLocation();
    }

    // MapCreator로 맵 파일을 로드하고 생성
    public boolean createMap(ImageLoader loader, String path) {
        this.imageLoader = loader;
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
    
    // playerId에 해당하는 플레이어 반환 (없으면 템플릿으로 생성)
    public Mario getPlayer(int playerId) {
        if (playerId < 1 || playerId > MAX_PLAYERS) {
            return null;
        }
        if (players[playerId] == null && map != null) {
            Mario template = map.getMario();
            if (template != null && imageLoader != null) {
                players[playerId] = new Mario(template.getX(), template.getY(), imageLoader);
                System.out.println("[MapManager] Created new Mario for player " + playerId + 
                                   " at (" + template.getX() + ", " + template.getY() + ")");
            }
        }
        return players[playerId];
    }
    
    public void setPlayer(int playerId, Mario player) {
        if (playerId >= 1 && playerId <= MAX_PLAYERS) {
            players[playerId] = player;
            System.out.println("[MapManager] Set player " + playerId + " (Mario object assigned)");
        }
    }
    
    public void removePlayer(int playerId) {
        if (playerId >= 1 && playerId <= MAX_PLAYERS) {
            players[playerId] = null;
            System.out.println("[MapManager] Removed player " + playerId);
        }
    }
    
    public Mario[] getAllPlayers() {
        return players;
    }
    
    // 특정 플레이어를 제외한 나머지 플레이어 목록 반환
    public ArrayList<Mario> getOtherPlayers(int myPlayerId) {
        ArrayList<Mario> others = new ArrayList<>();
        for (int i = 1; i <= MAX_PLAYERS; i++) {
            if (i != myPlayerId && players[i] != null) {
                others.add(players[i]);
            }
        }
        return others;
    }

    // 파이어볼 발사 (Fire Mario만 가능)
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

    // 깃발 도달 시 높이에 따른 보너스 점수 계산
    public int passMission() {
        if(getMario().getX() >= map.getEndPoint().getX() && !map.getEndPoint().isTouched()){
            map.getEndPoint().setTouched(true);
            int height = (int)getMario().getY();
            return height * 2;
        }
        else
            return -1;
    }

    // 깃발 지나서 스테이지 종료 체크
    public boolean endLevel(){
        return getMario().getX() >= map.getEndPoint().getX() + 320;
    }

    // 모든 충돌 검사 (서버에서 60Hz로 실행)
    public void checkCollisions(GameEngine engine) {
        if (map == null) {
            return;
        }

        for (int i = 1; i <= MAX_PLAYERS; i++) {
            if (players[i] != null) {
                checkBottomCollisions(players[i], engine);
                checkTopCollisions(players[i], engine);
                checkMarioHorizontalCollision(players[i], engine);
                checkPrizeContact(players[i], engine);
            }
        }
        
        checkEnemyCollisions();
        checkPrizeCollision();
        checkFireballContact();
    }

    // 하단 충돌: 블록 위 착지, 적 밟기
    private void checkBottomCollisions(Mario mario, GameEngine engine) {
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
                if (!mario.isDamageInvincible()) {
                    mario.acquirePoints(100);
                    toBeRemoved.add(enemy);
                    if (engine != null) {
                        engine.playStomp();
                    }
                }
            }
        }

        if (mario.getY() + mario.getDimension().height >= map.getBottomBorder()) {
            mario.setY(map.getBottomBorder() - mario.getDimension().height);
            mario.setFalling(false);
            mario.setVelY(0);
        }

        removeObjects(toBeRemoved);
    }

    // 상단 충돌: 블록 밑에서 부딪혀서 아이템 나오게 하기
    private void checkTopCollisions(Mario mario, GameEngine engine) {
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

    // 수평 충돌: 블록/적과 좌우 충돌, 적과 충돌 시 폼 변환 또는 사망
    private void checkMarioHorizontalCollision(Mario mario, GameEngine engine){
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
                if (!mario.isDamageInvincible()) {
                    marioDies = mario.onTouchEnemy(engine);
                }
            }
        }
        removeObjects(toBeRemoved);

        if (engine != null && mario.getX() <= engine.getCameraLocation().getX() && mario.getVelX() < 0) {
            mario.setVelX(0);
            mario.setX(engine.getCameraLocation().getX());
        }

        if(marioDies) {
            respawnPlayer(mario);
        }
    }

    // 적 충돌: 블록과 충돌 시 방향 전환, 떨어질 때 중력 적용
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

    // 아이템 충돌: BoostItem이 블록과 충돌하여 방향 전환, 바닥 착지
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

    // 아이템 획득: 마리오와 아이템 충돌 시 효과 적용
    private void checkPrizeContact(Mario mario, GameEngine engine) {
        ArrayList<Prize> prizes = map.getRevealedPrizes();
        ArrayList<GameObject> toBeRemoved = new ArrayList<>();

        Rectangle marioBounds = mario.getBounds();
        for(Prize prize : prizes){
            Rectangle prizeBounds = prize.getBounds();
            if (prizeBounds.intersects(marioBounds)) {
                prize.onTouch(mario, engine);
                toBeRemoved.add((GameObject) prize);
            } else if(prize instanceof Coin){
                prize.onTouch(mario, engine);
            }
        }

        removeObjects(toBeRemoved);
    }

    // 파이어볼 충돌: 적이나 블록에 닿으면 파이어볼 제거
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

    // 오브젝트 제거 (적, 파이어볼, 아이템)
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

    // 남은 시간 1초 감소 (서버에서 1Hz로 호출)
    public void updateTime(){
        if(map != null)
            map.updateTime(1);
    }

    public int getRemainingTime() {
        return (int)map.getRemainingTime();
    }
    
    // 현재 게임 상태를 GameStateMessage로 직렬화 (서버→클라이언트 전송용)
    public GameStateMessage collectGameState(Camera camera) {
        if (map == null) {
            return null;
        }

        GameStateMessage.PlayerState[] playerStates = new GameStateMessage.PlayerState[MAX_PLAYERS + 1];
        for (int i = 1; i <= MAX_PLAYERS; i++) {
            if (players[i] != null) {
                playerStates[i] = createPlayerState(players[i]);
            }
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

        ArrayList<Brick> bricks = map.getAllBricks();
        GameStateMessage.BrickState[] brickStates = new GameStateMessage.BrickState[bricks.size()];
        for (int i = 0; i < bricks.size(); i++) {
            Brick brick = bricks.get(i);
            GameStateMessage.BrickState bs = new GameStateMessage.BrickState();
            bs.x = (int) brick.getX();
            bs.y = (int) brick.getY();
            bs.type = brick.getClass().getSimpleName();
            bs.empty = brick.isEmpty();
            bs.breaking = false;
            if (brick instanceof OrdinaryBrick) {
                bs.breaking = ((OrdinaryBrick) brick).getFrames() > 0;
            }
            brickStates[i] = bs;
        }

        GameStateMessage.GameInfo gameInfo = new GameStateMessage.GameInfo();
        gameInfo.remainingTime = getRemainingTime();
        gameInfo.cameraX = camera != null ? camera.getX() : 0.0;
        gameInfo.mapName = "background.png";
        
        return new GameStateMessage(playerStates, enemyStates, itemStates, fireballStates, brickStates, gameInfo);
    }
    
    // 클라이언트 입력을 받아 서버에서 플레이어 조작
    public void processInput(int playerId, int keyCode, boolean pressed, GameEngine engine) {
        Mario mario = getPlayer(playerId);
        if (mario == null) {
            return;
        }
        
        if (pressed) {
            switch (keyCode) {
                case 38: // Up
                case 87: // W
                    mario.jump(engine);
                    break;
                case 39: // Right
                case 68: // D
                    mario.move(true, null);
                    break;
                case 37: // Left
                case 65: // A
                    mario.move(false, null);
                    break;
                case 32: // Space
                    fire(engine);
                    break;
            }
        } else {
            mario.setVelX(0);
        }
    }
    
    // 카메라 업데이트: 마리오가 화면 중앙을 넘으면 카메라 이동
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
    
    // Mario 객체를 PlayerState로 변환
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
        ps.damageInvincible = mario.isDamageInvincible();
        
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
    
    // 서버로부터 받은 게임 상태를 로컬 플레이어에 적용 (클라이언트용)
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
    
    // PlayerState를 Mario 객체에 적용
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
