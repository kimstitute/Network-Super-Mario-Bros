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

// 게임 엔진: 게임 루프, 상태 관리, 입력 처리, 네트워크 통신 총괄 (클라이언트)
public class GameEngine implements Runnable {

    private final static int WIDTH = 1268, HEIGHT = 708;

    private MapManager mapManager;
    private UIManager uiManager;
    private SoundManager soundManager;
    private GameStatus gameStatus;
    private boolean isRunning;
    private Camera camera;
    private ImageLoader imageLoader;
    private Thread thread;
    private StartScreenSelection startScreenSelection = StartScreenSelection.CREATE_ROOM;
    private int selectedMap = 0;
    private RankingManager rankingManager;
    private String playerName = "Mario" + (int)(Math.random() * 1000);
    private long gameStartTime;
    private NetworkManager networkManager;
    private int myPlayerId = 1;

    private GameEngine() {
        this.networkManager = new NetworkManager();
        init();
    }
    
    public GameEngine(NetworkManager.NetworkMode networkMode) {
        this.networkManager = new NetworkManager();
        init();
    }

    // 게임 시스템 초기화 및 JFrame 생성
    private void init() {
        imageLoader = new ImageLoader();
        InputManager inputManager = new InputManager(this);
        gameStatus = GameStatus.START_SCREEN;
        camera = new Camera();
        uiManager = new UIManager(this, WIDTH, HEIGHT);
        soundManager = new SoundManager();
        mapManager = new MapManager();
        rankingManager = new RankingManager();

        uiManager.addKeyListener(inputManager);
        uiManager.addMouseListener(inputManager);
        uiManager.setFocusable(true);
        
        JFrame frame = new JFrame("Super Mario Bros.");
        frame.add(uiManager);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        uiManager.requestFocusInWindow();

        start();
    }

    // 게임 루프 스레드 시작
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

    // 60Hz 게임 루프 (클라이언트는 렌더링만 수행, 서버가 게임 로직 처리)
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
        // RUNNING 상태에서만 repaint (서버로부터 받은 게임 상태 렌더링)
        if (gameStatus == GameStatus.RUNNING) {
            uiManager.repaint();
        }
    }

    private void gameLoop() {
        // 클라이언트는 게임 로직을 실행하지 않음 (서버가 처리)
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
        // 클라이언트는 서버로부터 GameState를 받아서 렌더링만 수행
    }

    // 게임 종료 처리: 랭킹 저장 및 결과 화면 표시
    private void handleGameEnd() {
        long gameEndTime = System.currentTimeMillis();
        double playTime = (gameEndTime - gameStartTime) / 1000.0;
        String completionTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        
        GameRecord record = new GameRecord(
            playerName,
            mapManager.getScore(),
            playTime,
            completionTime,
            mapManager.endLevel(),
            "Map " + (selectedMap + 1)
        );
        
        rankingManager.saveGameRecord(record);
        setGameStatus(GameStatus.RANKING_SCREEN);
    }

    // 카메라 업데이트: 마리오가 화면 중앙을 넘으면 카메라 이동
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

    // 입력 처리: 게임 상태에 따라 다른 처리
    public void receiveInput(ButtonAction input) {
        // 클라이언트는 항상 네트워크 모드로 동작
        if (networkManager != null) {
            handleNetworkInput(input);
            return;
        }

        if (gameStatus == GameStatus.START_SCREEN) {
            if (input == ButtonAction.SELECT && startScreenSelection == StartScreenSelection.CREATE_ROOM) {
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
        else if (gameStatus == GameStatus.STAGE_SELECTION) {
            if (input == ButtonAction.SELECT || input == ButtonAction.JUMP) {
                createRoomWithSelectedMap();
            } else if (input == ButtonAction.GO_UP) {
                selectStage(true);
            } else if (input == ButtonAction.GO_DOWN) {
                selectStage(false);
            } else if (input == ButtonAction.GO_TO_START_SCREEN) {
                setGameStatus(GameStatus.START_SCREEN);
            }
        }
        else if (gameStatus == GameStatus.RUNNING) {
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
        } else if (gameStatus == GameStatus.RANKING_SCREEN) {
            if (input == ButtonAction.PAUSE_RESUME) {
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
        uiManager.repaint();
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

    public long getGameOverTime() {
        return gameOverTime;
    }

    public StartScreenSelection getStartScreenSelection() {
        return startScreenSelection;
    }

    public void setGameStatus(GameStatus gameStatus) {
        System.out.println("[GAME] Game status changed: " + this.gameStatus + " -> " + gameStatus);
        this.gameStatus = gameStatus;
        uiManager.repaint();
    }

    public int getScore() {
        if (gameStatus == GameStatus.GAME_OVER) {
            return finalScore;
        }
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

    // 서버로부터 받은 게임 상태를 로컬에 적용 (생명이 0이면 게임오버 처리)
    public void applyGameState(GameStateMessage state) {
        mapManager.applyGameState(state);
        
        if (state != null && state.getPlayer(myPlayerId) != null) {
            GameStateMessage.PlayerState myPlayer = state.getPlayer(myPlayerId);
            if (myPlayer.lives <= 0 && gameStatus == GameStatus.RUNNING) {
                finalScore = myPlayer.points;
                gameOverTime = System.currentTimeMillis();
                System.out.println("[GAME] Client detected game over - Lives: " + myPlayer.lives + ", Final Score: " + finalScore);
                setGameStatus(GameStatus.GAME_OVER);
            }
        }
        
        uiManager.repaint();
    }
    
    // 서버가 게임 시작 신호를 보냈을 때 호출
    private void onGameStart() {
        System.out.println("[GAME] ===== onGameStart() CALLED =====");
        System.out.println("[GAME] Current game status: " + gameStatus);
        System.out.println("[GAME] Game start signal received from server");
        setGameStatus(GameStatus.RUNNING);
        System.out.println("[GAME] After setGameStatus, current status: " + gameStatus);
    }
    
    // 네트워크 모드 입력 처리: 서버에 입력 전송 또는 로컬 UI 업데이트
    private void handleNetworkInput(ButtonAction input) {
        if (gameStatus == GameStatus.START_SCREEN) {
            if (input == ButtonAction.SELECT && startScreenSelection == StartScreenSelection.CREATE_ROOM) {
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
            return;
        }
        
        if (gameStatus == GameStatus.ABOUT_SCREEN || gameStatus == GameStatus.HELP_SCREEN) {
            if (input == ButtonAction.GO_TO_START_SCREEN || input == ButtonAction.SELECT) {
                setGameStatus(GameStatus.START_SCREEN);
            }
            return;
        }
        
        if (gameStatus == GameStatus.STAGE_SELECTION) {
            if (input == ButtonAction.SELECT || input == ButtonAction.JUMP) {
                createRoomWithSelectedMap();
            } else if (input == ButtonAction.GO_UP) {
                selectStage(true);
            } else if (input == ButtonAction.GO_DOWN) {
                selectStage(false);
            } else if (input == ButtonAction.GO_TO_START_SCREEN) {
                setGameStatus(GameStatus.START_SCREEN);
            }
            return;
        }
        
        if (gameStatus == GameStatus.WAITING_FOR_PLAYERS) {
            if (input == ButtonAction.SELECT || input == ButtonAction.JUMP) {
                toggleReady();
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

        if (gameStatus != GameStatus.RUNNING) {
            return;
        }

        // 게임 플레이 중 입력을 서버로 전송
        int keyCode = buttonActionToKeyCode(input);
        boolean pressed = (input != ButtonAction.ACTION_COMPLETED);
        networkManager.sendInput(keyCode, pressed);
    }
    
    // ButtonAction을 KeyCode로 변환
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
    private boolean isPlayerReady = false;
    private int selectedStage = 0;
    private String selectedMapName = "Map 1.png";
    private int finalScore = 0;
    private long gameOverTime = 0;
    private int roomPlayerCount = 1;
    
    // 방 생성 플로우: 스테이지 선택 화면으로 이동
    private void createRoom() {
        System.out.println("[GAME] Opening stage selection...");
        selectedStage = 0;
        setGameStatus(GameStatus.STAGE_SELECTION);
    }
    
    // 선택된 맵으로 서버에 방 생성 요청
    private void createRoomWithSelectedMap() {
        System.out.println("[GAME] Creating room on server with map: " + selectedMapName);
        setGameStatus(GameStatus.CONNECTING_TO_SERVER);
        
        String serverAddress = "localhost";
        int serverPort = 25565;
        
        if (networkManager == null) {
            networkManager = new NetworkManager();
        }
        
        networkManager.setGameStateHandler(this::applyGameState);
        networkManager.setGameStartHandler(this::onGameStart);
        networkManager.setRoomInfoHandler(this::updateRoomInfo);
        
        if (networkManager.connectToServer(serverAddress, serverPort, playerName)) {
            System.out.println("[GAME] Connected to server, requesting room creation...");
            networkManager.sendCreateRoomRequest(selectedMapName);
            setGameStatus(GameStatus.WAITING_FOR_PLAYERS);
        } else {
            System.err.println("[GAME] Failed to connect to server");
            setGameStatus(GameStatus.START_SCREEN);
        }
    }
    
    // 스테이지 선택 (Stage 1 또는 Stage 2)
    public void selectStage(boolean up) {
        selectedStage = (selectedStage + (up ? -1 : 1) + 2) % 2;
        selectedMapName = (selectedStage == 0) ? "Map 1.png" : "Map 2.png";
        System.out.println("[GAME] Selected stage: " + (selectedStage + 1) + " (" + selectedMapName + ")");
        uiManager.repaint();
    }
    
    public int getSelectedStage() {
        return selectedStage;
    }
    
    // 방 참가 플로우: 서버 접속 화면으로 이동
    private void joinRoom() {
        System.out.println("[GAME] Joining room...");
        setGameStatus(GameStatus.CONNECTING_TO_SERVER);
        attemptServerConnection();
    }
    
    // 서버 접속 시도 및 JOIN_ROOM 요청
    private void attemptServerConnection() {
        System.out.println("[GAME] Attempting to connect to server...");

        if (networkManager == null) {
            networkManager = new NetworkManager();
        }

        String serverAddress = "localhost";
        int serverPort = 25565;
        String playerName = this.playerName;

        networkManager.setGameStateHandler(this::applyGameState);
        networkManager.setGameStartHandler(this::onGameStart);
        networkManager.setRoomInfoHandler(this::updateRoomInfo);

        if (networkManager.connectToServer(serverAddress, serverPort, playerName)) {
            System.out.println("[GAME] Connected to server, requesting to join room...");
            networkManager.sendJoinRoomRequest();
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
    
    public boolean isPlayerReady() {
        return isPlayerReady;
    }

    public int getRoomPlayerCount() {
        return roomPlayerCount;
    }

    // 서버로부터 받은 방 정보 업데이트 (플레이어 수, 방장 여부)
    public void updateRoomInfo(int playerCount, int hostClientId) {
        this.roomPlayerCount = playerCount;
        this.isRoomHost = (myPlayerId == hostClientId);
        System.out.println("[GAME] Room info updated - players: " + playerCount + ", isHost: " + isRoomHost);
        uiManager.repaint();
    }

    // READY 상태 토글 및 서버에 전송
    public void toggleReady() {
        isPlayerReady = !isPlayerReady;
        if (networkManager != null) {
            networkManager.sendReady();
        }
        System.out.println("[GAME] Player ready status: " + isPlayerReady);
        uiManager.repaint();
    }
    
    private void disconnectNetwork() {
        System.out.println("[GAME] Disconnecting network...");
        if (networkManager != null) {
            networkManager.disconnect();
        }
    }
}
