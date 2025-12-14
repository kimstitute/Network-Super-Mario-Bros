package manager;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import model.GameRecord;
import model.hero.Mario;
import network.NetworkManager;
import network.protocol.GameStateMessage;
import ranking.RankingManager;
import view.ImageLoader;
import view.StartScreenSelection;
import view.UIManager;

/**
 * 슈퍼 마리오 브라더스 게임의 핵심 엔진 클래스
 * 게임 루프, 상태 관리, 입력 처리, 렌더링 등 모든 게임 시스템을 총괄
 * Runnable 인터페이스를 구현하여 별도의 스레드에서 게임 루프 실행
 *
 * @author 네트워크프로그래밍 팀
 * @version 1.0
 * @since 2024-12-14
 */
public class GameEngine implements Runnable {

    /**
     * 게임 창의 고정 크기 (너비, 높이)
     * 1268x708 픽셀은 마리오 게임의 최적 해상도
     */
    private final static int WIDTH = 1268, HEIGHT = 708;

    /**
     * 맵 관리자 - 게임 맵, 오브젝트, 충돌 감지를 담당
     */
    private MapManager mapManager;
    
    /**
     * UI 관리자 - 화면 렌더링과 사용자 인터페이스를 담당
     */
    private UIManager uiManager;
    
    /**
     * 사운드 관리자 - 배경음악과 효과음을 담당
     */
    private SoundManager soundManager;
    
    /**
     * 현재 게임 상태 - 시작 화면, 맵 선택, 게임 플레이 등
     */
    private GameStatus gameStatus;
    
    /**
     * 게임 실행 상태 플래그
     */
    private boolean isRunning;
    
    /**
     * 카메라 - 마리오를 따라가는 화면 시점을 관리
     */
    private Camera camera;
    
    /**
     * 이미지 로더 - 모든 이미지 리소스를 로드하는 데 사용
     */
    private ImageLoader imageLoader;
    
    /**
     * 게임 루프를 실행하는 스레드
     */
    private Thread thread;
    
    /**
     * 시작 화면에서 현재 선택된 메뉴 항목
     */
    private StartScreenSelection startScreenSelection = StartScreenSelection.START_GAME;
    
    /**
     * 맵 선택 화면에서 현재 선택된 맵 인덱스
     */
    private int selectedMap = 0;

    /**
     * 랭킹 시스템 관리자
     */
    private RankingManager rankingManager;
    
    /**
     * 플레이어 이름
     */
    private String playerName = "Mario" + (int)(Math.random() * 1000);
    
    /**
     * 게임 시작 시간
     */
    private long gameStartTime;
    
    /**
     * 네트워크 관리자 - 멀티플레이어 기능을 담당
     */
    private NetworkManager networkManager;
    
    /**
     * 현재 플레이어 ID (1 또는 2)
     * 네트워크 모드에서 자신이 어떤 플레이어인지 식별
     */
    private int myPlayerId = 1;

    /**
     * GameEngine 생성자 (싱글톤 패턴)
     * init() 메서드를 호출하여 모든 시스템 초기화
     */
    private GameEngine() {
        this.networkManager = new NetworkManager();
        init();
    }
    
    /**
     * GameEngine 생성자 (네트워크 모드 지정)
     *
     * @param networkMode 네트워크 모드
     */
    public GameEngine(NetworkManager.NetworkMode networkMode) {
        this.networkManager = new NetworkManager();
        init();
        
        if (networkMode == NetworkManager.NetworkMode.CLIENT) {
            networkManager.setGameStateHandler(this::applyGameState);
        }
    }

    /**
     * 게임 엔진과 모든 하위 시스템을 초기화하는 메서드
     * 이미지 로더, 입력 관리자, UI, 사운드, 맵 관리자 등을 설정
     * 게임 창(JFrame)을 생성하고 설정
     */
    private void init() {
        // 핵심 시스템 초기화
        imageLoader = new ImageLoader();                    // 이미지 로더
        InputManager inputManager = new InputManager(this); // 입력 관리자
        gameStatus = GameStatus.START_SCREEN;               // 초기 상태: 시작 화면
        camera = new Camera();                             // 카메라
        uiManager = new UIManager(this, WIDTH, HEIGHT);    // UI 관리자
        soundManager = new SoundManager();                 // 사운드 관리자
        mapManager = new MapManager();                     // 맵 관리자
        rankingManager = new RankingManager();                 // 랭킹 관리자

        // 게임 창 설정
        JFrame frame = new JFrame("Super Mario Bros.");    // 창 제목
        frame.add(uiManager);                              // UI 패널 추가
        frame.addKeyListener(inputManager);                 // 키보드 리스너 추가
        frame.addMouseListener(inputManager);                // 마우스 리스너 추가
        frame.pack();                                      // 창 크기 자동 조정
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // 종료 시 프로그램 종료
        frame.setResizable(false);                         // 창 크기 변경 불가
        frame.setLocationRelativeTo(null);                  // 화면 중앙에 위치
        frame.setVisible(true);                            // 창 표시

        start();                                          // 게임 루프 시작
    }

    /**
     * 게임 루프 스레드를 시작하는 동기화 메서드
     * 중복 시작을 방지하고 스레드를 생성하여 시작
     */
    private synchronized void start() {
        if (isRunning)
            return;

        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    private void reset(){
        resetCamera();
        setGameStatus(GameStatus.START_SCREEN);
    }

    public void resetCamera(){
        camera = new Camera();
        soundManager.restartBackground();
    }

    public void selectMapViaMouse() {
        String path = uiManager.selectMapViaMouse(uiManager.getMousePosition());
        if (path != null) {
            createMap(path);
        }
    }

    public void selectMapViaKeyboard(){
        String path = uiManager.selectMapViaKeyboard(selectedMap);
        if (path != null) {
            createMap(path);
        }
    }

    public void changeSelectedMap(boolean up){
        selectedMap = uiManager.changeSelectedMap(selectedMap, up);
    }

    private void createMap(String path) {
        boolean loaded = mapManager.createMap(imageLoader, path);
        if(loaded){
            setGameStatus(GameStatus.RUNNING);
            soundManager.restartBackground();
            gameStartTime = System.currentTimeMillis(); // 게임 시작 시간 기록
        }

        else
            setGameStatus(GameStatus.START_SCREEN);
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();

        while (isRunning && !thread.isInterrupted()) {

            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                if (gameStatus == GameStatus.RUNNING) {
                    gameLoop();
                }
                delta--;
            }
            render();

            if(gameStatus != GameStatus.RUNNING){
                timer = System.currentTimeMillis();
            }

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                mapManager.updateTime();
            }
        }
    }

    private void render() {
        uiManager.repaint();
    }

    private void gameLoop() {
        if (networkManager != null && networkManager.getNetworkMode() == NetworkManager.NetworkMode.CLIENT) {
            clientGameLoop();
        } else {
            singlePlayerGameLoop();
        }
    }
    
    private void singlePlayerGameLoop() {
        updateLocations();
        checkCollisions();
        updateCamera();

        if (isGameOver()) {
            handleGameEnd();
        }

        int missionPassed = passMission();
        if(missionPassed > -1){
            mapManager.acquirePoints(missionPassed);
        } else if(mapManager.endLevel())
            setGameStatus(GameStatus.MISSION_PASSED);
    }

    private void clientGameLoop() {
        updateCamera();
    }

    /**
     * 게임 종료 처리 - 랭킹 저장
     */
    private void handleGameEnd() {
        // 게임 종료 시간 계산
        long gameEndTime = System.currentTimeMillis();
        double playTime = (gameEndTime - gameStartTime) / 1000.0;
        String completionTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        
        // 게임 기록 생성
        GameRecord record = new GameRecord(
            playerName,
            mapManager.getScore(),
            playTime,
            completionTime,
            mapManager.endLevel(),  // 스테이지 클리어 여부
            "Map " + (selectedMap + 1)
        );
        
        // 랭킹 저장
        rankingManager.saveGameRecord(record);
        
        // 랭킹 화면으로 이동
        setGameStatus(GameStatus.RANKING_SCREEN);
    }

    private void updateCamera() {
        Mario mario = mapManager.getMario();
        double marioVelocityX = mario.getVelX();
        double shiftAmount = 0;

        if (marioVelocityX > 0 && mario.getX() - 600 > camera.getX()) {
            shiftAmount = marioVelocityX;
        }

        camera.moveCam(shiftAmount, 0);
    }

    private void updateLocations() {
        mapManager.updateLocations();
    }

    private void checkCollisions() {
        mapManager.checkCollisions(this);
    }

    public void receiveInput(ButtonAction input) {
        // 네트워크 모드에서 입력 처리
        if (networkManager != null && networkManager.getNetworkMode() != NetworkManager.NetworkMode.SINGLE_PLAYER) {
            handleNetworkInput(input);
            return;
        }

        if (gameStatus == GameStatus.START_SCREEN) {
            if (input == ButtonAction.SELECT && startScreenSelection == StartScreenSelection.START_GAME) {
                startGame();
            } else if (input == ButtonAction.SELECT && startScreenSelection == StartScreenSelection.CREATE_ROOM) {
                createRoom();
            } else if (input == ButtonAction.SELECT && startScreenSelection == StartScreenSelection.JOIN_ROOM) {
                joinRoom();
            } else if (input == ButtonAction.SELECT && startScreenSelection == StartScreenSelection.VIEW_ABOUT) {
                setGameStatus(GameStatus.ABOUT_SCREEN);
            } else if (input == ButtonAction.SELECT && startScreenSelection == StartScreenSelection.VIEW_HELP) {
                setGameStatus(GameStatus.HELP_SCREEN);
            } else if (input == ButtonAction.GO_UP) {
                selectOption(true);
            } else if (input == ButtonAction.GO_DOWN) {
                selectOption(false);
            }
        }
        else if(gameStatus == GameStatus.MAP_SELECTION){
            if(input == ButtonAction.SELECT){
                selectMapViaKeyboard();
            }
            else if(input == ButtonAction.GO_UP){
                changeSelectedMap(true);
            }
            else if(input == ButtonAction.GO_DOWN){
                changeSelectedMap(false);
            }
        } else if (gameStatus == GameStatus.RUNNING) {
            Mario mario = mapManager.getMario();
            if (input == ButtonAction.JUMP) {
                mario.jump(this);
            } else if (input == ButtonAction.M_RIGHT) {
                mario.move(true, camera);
            } else if (input == ButtonAction.M_LEFT) {
                mario.move(false, camera);
            } else if (input == ButtonAction.ACTION_COMPLETED) {
                mario.setVelX(0);
            } else if (input == ButtonAction.FIRE) {
                mapManager.fire(this);
            } else if (input == ButtonAction.PAUSE_RESUME) {
                pauseGame();
            }
        } else if (gameStatus == GameStatus.PAUSED) {
            if (input == ButtonAction.PAUSE_RESUME) {
                pauseGame();
            }
        } else if (gameStatus == GameStatus.CONNECTING_TO_SERVER) {
            if (input == ButtonAction.SELECT || input == ButtonAction.JUMP) {
                attemptServerConnection();
            }
        } else if (gameStatus == GameStatus.WAITING_FOR_PLAYERS) {
            if (input == ButtonAction.SELECT || input == ButtonAction.JUMP) {
                if (networkManager != null && networkManager.getClientCount() >= 1) {
                    startGameWithConnectedPlayers();
                }
            }
        } else if (gameStatus == GameStatus.RANKING_SCREEN) {
            if (input == ButtonAction.PAUSE_RESUME) {
                setGameStatus(GameStatus.START_SCREEN);
            }
        } else if (gameStatus == GameStatus.WAITING_FOR_PLAYERS) {
            if (input == ButtonAction.PAUSE_RESUME) {
                startGameWithConnectedPlayers();
            } else if (input == ButtonAction.GO_TO_START_SCREEN) {
                disconnectNetwork();
                setGameStatus(GameStatus.START_SCREEN);
            }
        } else if (gameStatus == GameStatus.CONNECTING_TO_SERVER) {
            if (input == ButtonAction.PAUSE_RESUME) {
                attemptServerConnection();
            } else if (input == ButtonAction.GO_TO_START_SCREEN) {
                setGameStatus(GameStatus.START_SCREEN);
            }
        }
        else if(gameStatus == GameStatus.GAME_OVER && input == ButtonAction.GO_TO_START_SCREEN){
            reset();
        } else if(gameStatus == GameStatus.MISSION_PASSED && input == ButtonAction.GO_TO_START_SCREEN){
            reset();
        }

        if(input == ButtonAction.GO_TO_START_SCREEN){
            setGameStatus(GameStatus.START_SCREEN);
        }
    }

    private void selectOption(boolean selectUp) {
        startScreenSelection = startScreenSelection.select(selectUp);
    }

    private void startGame() {
        if (gameStatus != GameStatus.GAME_OVER) {
            setGameStatus(GameStatus.MAP_SELECTION);
        }
    }

    private void pauseGame() {
        if (gameStatus == GameStatus.RUNNING) {
            setGameStatus(GameStatus.PAUSED);
            soundManager.pauseBackground();
        } else if (gameStatus == GameStatus.PAUSED) {
            setGameStatus(GameStatus.RUNNING);
            soundManager.resumeBackground();
        }
    }

    public void shakeCamera(){
        camera.shakeCamera();
    }

    private boolean isGameOver() {
        if(gameStatus == GameStatus.RUNNING)
            return mapManager.isGameOver();
        return false;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public StartScreenSelection getStartScreenSelection() {
        return startScreenSelection;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public int getScore() {
        return mapManager.getScore();
    }

    public int getRemainingLives() {
        return mapManager.getRemainingLives();
    }

    public int getCoins() {
        return mapManager.getCoins();
    }

    public int getSelectedMap() {
        return selectedMap;
    }

    public void drawMap(Graphics2D g2) {
        mapManager.drawMap(g2);
    }

    public Point getCameraLocation() {
        return new Point((int)camera.getX(), (int)camera.getY());
    }

    private int passMission(){
        return mapManager.passMission();
    }

    public void playCoin() {
        soundManager.playCoin();
    }

    public void playOneUp() {
        soundManager.playOneUp();
    }

    public void playSuperMushroom() {
        soundManager.playSuperMushroom();
    }

    public void playMarioDies() {
        soundManager.playMarioDies();
    }

    public void playJump() {
        soundManager.playJump();
    }

    public void playFireFlower() {
        soundManager.playFireFlower();
    }

    public void playFireball() {
        soundManager.playFireball();
    }

    public void playStomp() {
        soundManager.playStomp();
    }

    public MapManager getMapManager() {
        return mapManager;
    }
    
    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public java.util.ArrayList<Mario> getOtherPlayers() {
        return mapManager.getOtherPlayers(myPlayerId);
    }

    public static void main(String... args) {
        new GameEngine();
    }

    public int getRemainingTime() {
        return mapManager.getRemainingTime();
    }
    
    private GameStateMessage collectGameState() {
        return mapManager.collectGameState(camera);
    }

    public void applyGameState(GameStateMessage state) {
        mapManager.applyGameState(state);
    }
    
    private void handleNetworkInput(ButtonAction input) {
        if (gameStatus == GameStatus.WAITING_FOR_PLAYERS) {
            if (input == ButtonAction.SELECT && isRoomHost) {
                startGameWithConnectedPlayers();
            } else if (input == ButtonAction.GO_TO_START_SCREEN) {
                disconnectNetwork();
                setGameStatus(GameStatus.START_SCREEN);
            }
            return;
        }
        
        if (gameStatus == GameStatus.CONNECTING_TO_SERVER) {
            if (input == ButtonAction.SELECT) {
                attemptServerConnection();
            } else if (input == ButtonAction.GO_TO_START_SCREEN) {
                setGameStatus(GameStatus.START_SCREEN);
            }
            return;
        }
        
        if (gameStatus == GameStatus.MAP_SELECTION) {
            if (input == ButtonAction.SELECT) {
                selectMapViaKeyboard();
            } else if (input == ButtonAction.GO_UP) {
                changeSelectedMap(true);
            } else if (input == ButtonAction.GO_DOWN) {
                changeSelectedMap(false);
            }
            return;
        }
        
        if (gameStatus != GameStatus.RUNNING) {
            return;
        }
        
        int keyCode = buttonActionToKeyCode(input);
        boolean pressed = (input != ButtonAction.ACTION_COMPLETED);
        networkManager.sendInput(keyCode, pressed);
    }
    
    private int buttonActionToKeyCode(ButtonAction action) {
        switch (action) {
            case JUMP: return 38;
            case M_RIGHT: return 39;
            case M_LEFT: return 37;
            case FIRE: return 32;
            case ACTION_COMPLETED: return 0;
            default: return -1;
        }
    }
    
    public void setMyPlayerId(int playerId) {
        this.myPlayerId = playerId;
    }
    
    public int getMyPlayerId() {
        return myPlayerId;
    }
    
    private boolean isRoomHost = false;
    
    private void createRoom() {
        System.out.println("[GAME] Creating room...");
        
        if (networkManager == null) {
            networkManager = new NetworkManager();
        }
        
        String serverAddress = "localhost";
        int serverPort = 25565;
        String playerName = this.playerName;
        
        if (networkManager.connectToServer(serverAddress, serverPort, playerName)) {
            System.out.println("[GAME] Connected to server");
            networkManager.setGameStateHandler(this::applyGameState);
            isRoomHost = true;
            setGameStatus(GameStatus.WAITING_FOR_PLAYERS);
        } else {
            System.err.println("[GAME] Failed to connect to server");
            setGameStatus(GameStatus.START_SCREEN);
        }
    }
    
    private void joinRoom() {
        System.out.println("[GAME] Joining room...");
        setGameStatus(GameStatus.CONNECTING_TO_SERVER);
        attemptServerConnection();
    }
    
    private void attemptServerConnection() {
        System.out.println("[GAME] Attempting to connect to server...");
        
        if (networkManager == null) {
            networkManager = new NetworkManager();
        }
        
        String serverAddress = "localhost";
        int serverPort = 25565;
        String playerName = this.playerName;
        
        if (networkManager.connectToServer(serverAddress, serverPort, playerName)) {
            System.out.println("[GAME] Connected to server successfully");
            networkManager.setGameStateHandler(this::applyGameState);
            isRoomHost = false;
            setGameStatus(GameStatus.WAITING_FOR_PLAYERS);
        } else {
            System.err.println("[GAME] Failed to connect to server");
            setGameStatus(GameStatus.START_SCREEN);
        }
    }
    
    public boolean isRoomHost() {
        return isRoomHost;
    }
    
    private void startGameWithConnectedPlayers() {
        System.out.println("[GAME] Starting game with connected players...");
        setGameStatus(GameStatus.MAP_SELECTION);
    }
    
    private void disconnectNetwork() {
        System.out.println("[GAME] Disconnecting network...");
        if (networkManager != null) {
            networkManager.disconnect();
        }
    }
}
