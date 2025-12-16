package view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.*;
import manager.GameEngine;
import manager.GameStatus;
import model.GameRecord;
import ranking.RankingManager;

// UI 관리자: 모든 화면 렌더링 처리 (시작 화면, 게임 플레이, HUD, 리더보드 등)
public class UIManager extends JPanel{

    private GameEngine engine;
    private Font gameFont;
    private BufferedImage startScreenImage, aboutScreenImage, helpScreenImage, gameOverScreen;
    private BufferedImage heartIcon;
    private BufferedImage coinIcon;
    private BufferedImage selectIcon;
    private BufferedImage spriteSheet;
    private BufferedImage mapBackgroundImage;
    private String currentMapName = null;
    private MapSelection mapSelection;
    private java.util.Map<Integer, Animation> playerAnimations;

    public UIManager(GameEngine engine, int width, int height) {
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));

        this.engine = engine;
        ImageLoader loader = engine.getImageLoader();

        mapSelection = new MapSelection();
        playerAnimations = new java.util.HashMap<>();

        this.spriteSheet = loader.loadImage("/sprite.png");
        this.heartIcon = loader.loadImage("/heart-icon.png");
        this.coinIcon = loader.getSubImage(spriteSheet, 1, 5, 48, 48);
        this.selectIcon = loader.loadImage("/select-icon.png");
        this.startScreenImage = loader.loadImage("/start-screen.png");
        this.helpScreenImage = loader.loadImage("/help-screen.png");
        this.aboutScreenImage = loader.loadImage("/about-screen.png");
        this.gameOverScreen = loader.loadImage("/game-over.png");

        try {
            InputStream in = getClass().getResourceAsStream("/media/font/mario-font.ttf");
            gameFont = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (FontFormatException | IOException e) {
            gameFont = new Font("Verdana", Font.PLAIN, 12);
            e.printStackTrace();
        }
    }

    // 게임 상태에 따라 적절한 화면 렌더링
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        GameStatus gameStatus = engine.getGameStatus();

        if(gameStatus == GameStatus.START_SCREEN){
            drawStartScreen(g2);
        }
        else if(gameStatus == GameStatus.MAP_SELECTION){
            drawMapSelectionScreen(g2);
        }
        else if(gameStatus == GameStatus.ABOUT_SCREEN){
            drawAboutScreen(g2);
        }
        else if(gameStatus == GameStatus.HELP_SCREEN){
            drawHelpScreen(g2);
        }
        else if(gameStatus == GameStatus.GAME_OVER){
            drawGameOverScreen(g2);
        }
        else if(gameStatus == GameStatus.RANKING_SCREEN){
            drawRankingScreen(g2);
        }
        else if(gameStatus == GameStatus.STAGE_SELECTION){
            drawStageSelectionScreen(g2);
        }
        else if(gameStatus == GameStatus.WAITING_FOR_PLAYERS){
            drawWaitingForPlayersScreen(g2);
        }
        else if(gameStatus == GameStatus.CONNECTING_TO_SERVER){
            drawConnectingScreen(g2);
        }
        else {
            System.out.println("[UI] Drawing game screen");
            if(engine.getNetworkManager() != null && 
               engine.getNetworkManager().getNetworkMode() == network.NetworkManager.NetworkMode.CLIENT) {
                System.out.println("[UI] CLIENT MODE - calling drawGameFromState()");
                drawGameFromState(g2);
            } else {
                System.out.println("[UI] SINGLE/SERVER MODE - drawing traditional way");
                Point camLocation = engine.getCameraLocation();
                g2.translate(-camLocation.x, -camLocation.y);
                engine.drawMap(g2);
                
                drawOtherPlayers(g2);
                
                g2.translate(camLocation.x, camLocation.y);

                drawPoints(g2);
                drawRemainingLives(g2);
                drawAcquiredCoins(g2);
                drawRemainingTime(g2);

                if(gameStatus == GameStatus.PAUSED){
                    drawPauseScreen(g2);
                }
                else if(gameStatus == GameStatus.MISSION_PASSED){
                    drawVictoryScreen(g2);
                }
            }
        }

        g2.dispose();
    }

    // 랭킹 화면: 점수 순위와 시간 순위 표시
    private void drawRankingScreen(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(Color.YELLOW);
        g2.setFont(gameFont.deriveFont(40f));
        String title = "랭킹 시스템";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (getWidth() - titleWidth) / 2, 50);

        RankingManager rankingManager = new RankingManager();

        drawRankingList(g2, "점수 순위", rankingManager.getScoreRanking(), 150, new Color(255, 215, 0));
        drawRankingList(g2, "시간 순위", rankingManager.getTimeRanking(), 400, Color.CYAN);

        g2.setColor(Color.WHITE);
        g2.setFont(gameFont.deriveFont(20f));
        String backMessage = "SPACE 키를 누르면 메뉴로 돌아갑니다";
        int backWidth = g2.getFontMetrics().stringWidth(backMessage);
        g2.drawString(backMessage, (getWidth() - backWidth) / 2, getHeight() - 50);
    }

    // 순위 리스트 렌더링
    private void drawRankingList(Graphics2D g2, String title, java.util.List<GameRecord> ranking, int startY, Color titleColor) {
        g2.setColor(titleColor);
        g2.setFont(gameFont.deriveFont(25f));
        g2.drawString(title, 100, startY);

        g2.setColor(Color.WHITE);
        g2.setFont(gameFont.deriveFont(18f));

        for (int i = 0; i < Math.min(ranking.size(), 10); i++) {
            GameRecord record = ranking.get(i);
            int y = startY + 40 + (i * 25);

            String rankIcon = getRankIcon(i);
            g2.drawString(rankIcon, 120, y);

            String playerInfo = String.format("%s - %d점 (%s)", 
                record.getPlayerName(), 
                record.getScore(), 
                record.getFormattedTime());
            g2.drawString(playerInfo, 160, y);
        }

        if (ranking.isEmpty()) {
            g2.drawString("아직 기록이 없습니다", 120, startY + 40);
        }
    }

    // 순위 아이콘 반환 (1위, 2위, 3위, 그 외)
    private String getRankIcon(int rank) {
        switch (rank) {
            case 0: return "1위";
            case 1: return "2위";
            case 2: return "3위";
            default: return String.format("%d위", rank + 1);
        }
    }

    private void drawRemainingTime(Graphics2D g2) {
        g2.setFont(gameFont.deriveFont(25f));
        g2.setColor(Color.WHITE);
        String displayedStr = "TIME: " + engine.getRemainingTime();
        g2.drawString(displayedStr, 750, 50);
    }

    private void drawVictoryScreen(Graphics2D g2) {
        g2.setFont(gameFont.deriveFont(50f));
        g2.setColor(Color.WHITE);
        String displayedStr = "YOU WON!";
        int stringLength = g2.getFontMetrics().stringWidth(displayedStr);
        g2.drawString(displayedStr, (getWidth()-stringLength)/2, getHeight()/2);
    }

    private void drawHelpScreen(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        g2.setFont(gameFont.deriveFont(50f));
        g2.setColor(Color.YELLOW);
        String title = "HOW TO PLAY";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (getWidth() - titleWidth) / 2, 80);
        
        g2.setFont(gameFont.deriveFont(24f));
        g2.setColor(Color.WHITE);
        
        String[] instructions = {
            "",
            "MOVEMENT:",
            "  LEFT/RIGHT ARROW - Move Mario",
            "  UP ARROW - Jump",
            "  SPACE - Fire (when powered up)",
            "",
            "MULTIPLAYER:",
            "  CREATE ROOM - Start a new game",
            "  SELECT STAGE - Choose Stage 1 or 2",
            "  READY - Signal you're ready to start",
            "  JOIN ROOM - Join existing game",
            "",
            "OBJECTIVE:",
            "  Reach the flag at the end",
            "  Collect coins and defeat enemies",
            "  Compete with other players!",
            "",
            ""
        };
        
        int y = 150;
        for (String line : instructions) {
            g2.drawString(line, 100, y);
            y += 35;
        }
        
        g2.setFont(gameFont.deriveFont(20f));
        g2.setColor(Color.GRAY);
        String backMsg = "Press ESC to return";
        int backWidth = g2.getFontMetrics().stringWidth(backMsg);
        g2.drawString(backMsg, (getWidth() - backWidth) / 2, getHeight() - 40);
    }

    private void drawAboutScreen(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        g2.setFont(gameFont.deriveFont(50f));
        g2.setColor(Color.YELLOW);
        String title = "ABOUT";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (getWidth() - titleWidth) / 2, 100);
        
        g2.setFont(gameFont.deriveFont(30f));
        g2.setColor(Color.WHITE);
        String gameTitle = "Super Mario Bros.";
        int gameTitleWidth = g2.getFontMetrics().stringWidth(gameTitle);
        g2.drawString(gameTitle, (getWidth() - gameTitleWidth) / 2, 180);
        
        g2.setFont(gameFont.deriveFont(20f));
        g2.setColor(new Color(150, 150, 150));
        String subtitle = "Multiplayer Network Edition";
        int subtitleWidth = g2.getFontMetrics().stringWidth(subtitle);
        g2.drawString(subtitle, (getWidth() - subtitleWidth) / 2, 220);
        
        g2.setFont(gameFont.deriveFont(24f));
        g2.setColor(Color.WHITE);
        
        String[] aboutText = {
            "",
            "Developed by:",
            "Kim Minsang",
            "",
            "Hansung University",
            "Computer Science / Artificial Intelligence",
            "3rd Year",
            "",
            "Network Programming Course",
            "Fall 2025",
            "",
            ""
        };
        
        int y = 280;
        for (String line : aboutText) {
            int lineWidth = g2.getFontMetrics().stringWidth(line);
            g2.drawString(line, (getWidth() - lineWidth) / 2, y);
            y += 35;
        }
        
        g2.setFont(gameFont.deriveFont(20f));
        g2.setColor(Color.GRAY);
        String backMsg = "Press ESC to return";
        int backWidth = g2.getFontMetrics().stringWidth(backMsg);
        g2.drawString(backMsg, (getWidth() - backWidth) / 2, getHeight() - 40);
    }

    private void drawGameOverScreen(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        g2.setFont(gameFont.deriveFont(80f));
        g2.setColor(Color.WHITE);
        String gameOverText = "GAME OVER";
        int textWidth = g2.getFontMetrics().stringWidth(gameOverText);
        g2.drawString(gameOverText, (getWidth() - textWidth) / 2, getHeight() / 2);
        
        g2.setFont(gameFont.deriveFont(30f));
        String pressKeyText = "Press any key to exit";
        int pressKeyWidth = g2.getFontMetrics().stringWidth(pressKeyText);
        g2.drawString(pressKeyText, (getWidth() - pressKeyWidth) / 2, getHeight() / 2 + 80);
    }

    private void drawPauseScreen(Graphics2D g2) {
        g2.setFont(gameFont.deriveFont(50f));
        g2.setColor(Color.WHITE);
        String displayedStr = "PAUSED";
        int stringLength = g2.getFontMetrics().stringWidth(displayedStr);
        g2.drawString(displayedStr, (getWidth()-stringLength)/2, getHeight()/2);
    }

    private void drawAcquiredCoins(Graphics2D g2) {
        g2.setFont(gameFont.deriveFont(30f));
        g2.setColor(Color.WHITE);
        String displayedStr = "" + engine.getCoins();
        g2.drawImage(coinIcon, getWidth()-115, 10, null);
        g2.drawString(displayedStr, getWidth()-65, 50);
    }

    private void drawRemainingLives(Graphics2D g2) {
        g2.setFont(gameFont.deriveFont(30f));
        g2.setColor(Color.WHITE);
        String displayedStr = "" + engine.getRemainingLives();
        g2.drawImage(heartIcon, 50, 10, null);
        g2.drawString(displayedStr, 100, 50);
    }

    private void drawPoints(Graphics2D g2){
        g2.setFont(gameFont.deriveFont(25f));
        g2.setColor(Color.WHITE);
        String displayedStr = "Points: " + engine.getScore();
        g2.drawString(displayedStr, 300, 50);
    }

    private void drawStartScreen(Graphics2D g2){
        int row = engine.getStartScreenSelection().getLineNumber();
        g2.drawImage(startScreenImage, 0, 0, null);

        g2.setFont(gameFont.deriveFont(16f));
        g2.setColor(new Color(200, 200, 200));
        String credit = "Hansung Univ CS/AI Kim Minsang";
        int creditWidth = g2.getFontMetrics().stringWidth(credit);
        g2.drawString(credit, (getWidth() - creditWidth) / 2, 390);

        g2.setFont(gameFont.deriveFont(30f));
        g2.setColor(Color.WHITE);

        String[] menuItems = {
            "CREATE ROOM",
            "JOIN ROOM",
            "HOW TO PLAY",
            "ABOUT"
        };

        int startY = 440;
        int lineHeight = 70;

        for (int i = 0; i < menuItems.length; i++) {
            int y = startY + i * lineHeight;
            g2.drawString(menuItems[i], 450, y + 35);
        }

        g2.drawImage(selectIcon, 375, row * lineHeight + startY, null);
    }

    private void drawMapSelectionScreen(Graphics2D g2){
        g2.setFont(gameFont.deriveFont(50f));
        g2.setColor(Color.WHITE);
        mapSelection.draw(g2);
        int row = engine.getSelectedMap();
        int y_location = row*100+300-selectIcon.getHeight();
        g2.drawImage(selectIcon, 375, y_location, null);
    }

    public String selectMapViaMouse(Point mouseLocation) {
        return mapSelection.selectMap(mouseLocation);
    }

    public String selectMapViaKeyboard(int index){
        return mapSelection.selectMap(index);
    }

    public int changeSelectedMap(int index, boolean up){
        return mapSelection.changeSelectedMap(index, up);
    }

    public Point getMousePosition() {
        return super.getMousePosition();
    }
    
    // 게임 방 대기 화면 렌더링
    private void drawWaitingForPlayersScreen(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setFont(gameFont.deriveFont(50f));
        g2.setColor(Color.YELLOW);
        String title = "GAME ROOM";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (getWidth() - titleWidth) / 2, 100);

        boolean isHost = engine.isRoomHost();
        boolean isReady = engine.isPlayerReady();
        int playerCount = engine.getRoomPlayerCount();

        g2.setFont(gameFont.deriveFont(35f));
        g2.setColor(Color.CYAN);
        String playerInfo = String.format("Players: %d / 4", playerCount);
        int playerInfoWidth = g2.getFontMetrics().stringWidth(playerInfo);
        g2.drawString(playerInfo, (getWidth() - playerInfoWidth) / 2, 180);

        g2.setFont(gameFont.deriveFont(25f));
        if (isHost) {
            g2.setColor(Color.ORANGE);
            String roleInfo = "You are the HOST";
            int roleInfoWidth = g2.getFontMetrics().stringWidth(roleInfo);
            g2.drawString(roleInfo, (getWidth() - roleInfoWidth) / 2, 240);
        } else {
            g2.setColor(Color.WHITE);
            String roleInfo = "Waiting for host...";
            int roleInfoWidth = g2.getFontMetrics().stringWidth(roleInfo);
            g2.drawString(roleInfo, (getWidth() - roleInfoWidth) / 2, 240);
        }
        
        g2.setFont(gameFont.deriveFont(30f));
        if (isReady) {
            g2.setColor(Color.GREEN);
            String readyText = "READY!";
            int readyWidth = g2.getFontMetrics().stringWidth(readyText);
            g2.drawString(readyText, (getWidth() - readyWidth) / 2, 320);
        } else {
            g2.setColor(Color.RED);
            String notReadyText = "NOT READY";
            int notReadyWidth = g2.getFontMetrics().stringWidth(notReadyText);
            g2.drawString(notReadyText, (getWidth() - notReadyWidth) / 2, 320);
        }
        
        g2.setFont(gameFont.deriveFont(20f));
        g2.setColor(Color.GRAY);
        String instruction = "Press SPACE to toggle READY";
        int instructionWidth = g2.getFontMetrics().stringWidth(instruction);
        g2.drawString(instruction, (getWidth() - instructionWidth) / 2, 380);

        g2.setFont(gameFont.deriveFont(22f));
        g2.setColor(Color.LIGHT_GRAY);
        
        if (isHost) {
            String[] instructions = {
                "",
                "How to start:",
                "1. Wait for other players to join",
                "2. Press SPACE to start game",
                "",
                "Or start with 1 player (solo)",
                ""
            };
            int startY = 320;
            for (int i = 0; i < instructions.length; i++) {
                int textWidth = g2.getFontMetrics().stringWidth(instructions[i]);
                g2.drawString(instructions[i], (getWidth() - textWidth) / 2, startY + i * 35);
            }
            
            g2.setFont(gameFont.deriveFont(28f));
            g2.setColor(Color.GREEN);
            String startMsg = "Press SPACE to start game";
            int startWidth = g2.getFontMetrics().stringWidth(startMsg);
            g2.drawString(startMsg, (getWidth() - startWidth) / 2, 560);
        } else {
            String[] instructions = {
                "",
                "Waiting for host to start the game...",
                "",
                "Players in room will be shown here",
                ""
            };
            int startY = 320;
            for (int i = 0; i < instructions.length; i++) {
                int textWidth = g2.getFontMetrics().stringWidth(instructions[i]);
                g2.drawString(instructions[i], (getWidth() - textWidth) / 2, startY + i * 35);
            }
        }

        g2.setFont(gameFont.deriveFont(20f));
        g2.setColor(Color.GRAY);
        String backMsg = "ESC to return to menu";
        int backWidth = g2.getFontMetrics().stringWidth(backMsg);
        g2.drawString(backMsg, (getWidth() - backWidth) / 2, getHeight() - 50);
    }
    
    // 스테이지 선택 화면 렌더링
    private void drawStageSelectionScreen(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        g2.setFont(gameFont.deriveFont(50f));
        g2.setColor(Color.YELLOW);
        String title = "SELECT STAGE";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (getWidth() - titleWidth) / 2, 100);
        
        int selectedStage = engine.getSelectedStage();
        
        g2.setFont(gameFont.deriveFont(40f));
        
        String[] stages = {"STAGE 1", "STAGE 2"};
        int startY = 250;
        
        for (int i = 0; i < stages.length; i++) {
            if (i == selectedStage) {
                g2.setColor(Color.GREEN);
            } else {
                g2.setColor(Color.WHITE);
            }
            
            String stageName = stages[i];
            int stageWidth = g2.getFontMetrics().stringWidth(stageName);
            g2.drawString(stageName, (getWidth() - stageWidth) / 2, startY + i * 80);
            
            if (i == selectedStage) {
                int iconY = startY + i * 80 - selectIcon.getHeight() / 2 - 10;
                g2.drawImage(selectIcon, (getWidth() - stageWidth) / 2 - 60, iconY, null);
            }
        }
        
        g2.setFont(gameFont.deriveFont(20f));
        g2.setColor(Color.GRAY);
        
        String[] instructions = {
            "Use ARROW KEYS to select",
            "Press SPACE to confirm",
            "Press ESC to return"
        };
        
        int instructionY = 500;
        for (int i = 0; i < instructions.length; i++) {
            int textWidth = g2.getFontMetrics().stringWidth(instructions[i]);
            g2.drawString(instructions[i], (getWidth() - textWidth) / 2, instructionY + i * 30);
        }
    }
    
    // 서버 접속 중 화면 렌더링
    private void drawConnectingScreen(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        g2.setFont(gameFont.deriveFont(50f));
        g2.setColor(Color.YELLOW);
        String title = "JOIN ROOM";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (getWidth() - titleWidth) / 2, 150);
        
        g2.setFont(gameFont.deriveFont(30f));
        g2.setColor(Color.WHITE);
        
        String[] messages = {
            "Connecting to server...",
            "",
            "Server: localhost:25565",
            "",
            "Make sure:",
            "1. Server is running",
            "2. A room has been created",
            "",
            "Press SPACE to connect",
            ""
        };
        
        int startY = 250;
        for (int i = 0; i < messages.length; i++) {
            int textWidth = g2.getFontMetrics().stringWidth(messages[i]);
            g2.drawString(messages[i], (getWidth() - textWidth) / 2, startY + i * 40);
        }
        
        g2.setFont(gameFont.deriveFont(20f));
        g2.setColor(Color.GRAY);
        String backMsg = "ESC to return to menu";
        int backWidth = g2.getFontMetrics().stringWidth(backMsg);
        g2.drawString(backMsg, (getWidth() - backWidth) / 2, getHeight() - 50);
    }
    
    // 다른 플레이어들을 반투명하게 렌더링 (네트워크 멀티플레이어)
    private void drawOtherPlayers(Graphics2D g2) {
        java.util.ArrayList<model.hero.Mario> otherPlayers = engine.getOtherPlayers();
        
        if (otherPlayers == null || otherPlayers.isEmpty()) {
            return;
        }
        
        Composite originalComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        
        for (model.hero.Mario player : otherPlayers) {
            if (player != null) {
                player.draw(g2);
                drawPlayerLabel(g2, player, "Player");
            }
        }
        
        g2.setComposite(originalComposite);
    }
    
    // 플레이어 위에 라벨 표시
    private void drawPlayerLabel(Graphics2D g2, model.hero.Mario player, String label) {
        g2.setFont(gameFont.deriveFont(12f));
        g2.setColor(Color.WHITE);
        
        int x = (int) player.getX();
        int y = (int) player.getY() - 10;
        
        g2.setColor(Color.BLACK);
        g2.drawString(label, x - 1, y);
        g2.drawString(label, x + 1, y);
        g2.drawString(label, x, y - 1);
        g2.drawString(label, x, y + 1);
        
        g2.setColor(Color.WHITE);
        g2.drawString(label, x, y);
    }
    
    // 서버로부터 받은 GameStateMessage 기반 게임 화면 렌더링
    private void drawGameFromState(Graphics2D g2) {
        System.out.println("[UI] drawGameFromState() called");
        network.protocol.GameStateMessage gameState = engine.getNetworkManager().getLatestGameState();
        System.out.println("[UI] GameState from network: " + (gameState != null ? "EXISTS" : "NULL"));
        
        if (gameState == null) {
            System.out.println("[UI] GameState is null, showing waiting message");
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.WHITE);
            g2.setFont(gameFont.deriveFont(30f));
            String msg = "서버로부터 게임 상태를 받는 중...";
            int msgWidth = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() / 2);
            return;
        }
        
        int myPlayerId = engine.getMyPlayerId();
        System.out.println("[UI] ===== CAMERA DEBUG =====");
        System.out.println("[UI] My Player ID: " + myPlayerId);
        network.protocol.GameStateMessage.PlayerState myPlayer = gameState.getPlayer(myPlayerId);
        
        if (myPlayer == null) {
            System.out.println("[UI] WARNING: myPlayer is NULL for ID " + myPlayerId);
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
            return;
        }
        
        System.out.println("[UI] My Player position: x=" + myPlayer.x + ", y=" + myPlayer.y);
        int cameraX = myPlayer.x - getWidth() / 2;
        System.out.println("[UI] Camera X: " + cameraX);
        int cameraY = 0;
        if (cameraX < 0) cameraX = 0;
        
        g2.translate(-cameraX, -cameraY);
        
        g2.setColor(new Color(92, 148, 252));
        g2.fillRect(cameraX, cameraY, getWidth(), getHeight());
        
        if (gameState.getGameInfo() != null && gameState.getGameInfo().mapName != null) {
            String backgroundName = gameState.getGameInfo().mapName;
            if (mapBackgroundImage == null || !backgroundName.equals(currentMapName)) {
                currentMapName = backgroundName;
                mapBackgroundImage = engine.getImageLoader().loadImage("/" + backgroundName);
            }
        }
        
        if (mapBackgroundImage != null) {
            g2.drawImage(mapBackgroundImage, 0, 0, null);
        }
        
        drawBricksFromState(g2, gameState);
        drawPlayersFromState(g2, gameState, myPlayerId);
        drawEnemiesFromState(g2, gameState);
        drawItemsFromState(g2, gameState);
        
        g2.translate(cameraX, cameraY);
        
        drawHUDFromState(g2, myPlayer, gameState);
    }
    
    // 플레이어들 렌더링 (GameStateMessage 기반)
    private void drawPlayersFromState(Graphics2D g2, network.protocol.GameStateMessage gameState, int myPlayerId) {
        network.protocol.GameStateMessage.PlayerState[] players = gameState.getPlayers();
        if (players == null) return;

        ImageLoader loader = engine.getImageLoader();

        for (int i = 1; i < players.length; i++) {
            network.protocol.GameStateMessage.PlayerState player = players[i];
            if (player == null) continue;

            Animation animation = playerAnimations.get(i);
            if (animation == null) {
                animation = new Animation(loader.getLeftFrames(0), loader.getRightFrames(0));
                playerAnimations.put(i, animation);
            }

            BufferedImage playerImage = null;
            try {
                boolean movingInX = Math.abs(player.velX) > 0;
                boolean movingInY = player.jumping || Math.abs(player.velY) > 0;
                
                if (movingInY && player.toRight) {
                    playerImage = animation.getRightFrames()[0];
                } else if (movingInY) {
                    playerImage = animation.getLeftFrames()[0];
                } else if (movingInX) {
                    playerImage = animation.animate(5, player.toRight);
                } else {
                    BufferedImage[] frames = player.toRight ? animation.getRightFrames() : animation.getLeftFrames();
                    if (frames != null && frames.length > 1) {
                        playerImage = frames[1];
                    }
                }
            } catch (Exception e) {
                System.err.println("[UI] Error loading player sprite: " + e.getMessage());
            }

            if (playerImage != null) {
                g2.drawImage(playerImage, player.x, player.y, null);
            } else {
                g2.setColor(i == myPlayerId ? Color.RED : Color.BLUE);
                g2.fillRect(player.x, player.y, 48, 48);
            }
            
            g2.setFont(gameFont.deriveFont(Font.BOLD, 14f));
            String playerLabel = "P" + i;
            int labelWidth = g2.getFontMetrics().stringWidth(playerLabel);
            int labelX = player.x + (48 - labelWidth) / 2;
            int labelY = player.y - 5;
            
            g2.setColor(Color.BLACK);
            g2.drawString(playerLabel, labelX - 1, labelY - 1);
            g2.drawString(playerLabel, labelX + 1, labelY - 1);
            g2.drawString(playerLabel, labelX - 1, labelY + 1);
            g2.drawString(playerLabel, labelX + 1, labelY + 1);
            
            Color labelColor = (i == myPlayerId) ? Color.YELLOW : Color.WHITE;
            g2.setColor(labelColor);
            g2.drawString(playerLabel, labelX, labelY);
        }
    }
    
    // 적들 렌더링 (GameStateMessage 기반)
    private void drawEnemiesFromState(Graphics2D g2, network.protocol.GameStateMessage gameState) {
        network.protocol.GameStateMessage.EnemyState[] enemies = gameState.getEnemies();
        if (enemies == null) return;
        
        ImageLoader loader = engine.getImageLoader();
        
        for (network.protocol.GameStateMessage.EnemyState enemy : enemies) {
            if (enemy == null || !enemy.alive) continue;
            
            BufferedImage enemyImage = null;
            if (enemy.type != null && enemy.type.contains("Goomba")) {
                int col = enemy.direction ? 5 : 2;
                enemyImage = loader.getSubImage(spriteSheet, col, 4, 48, 48);
            } else if (enemy.type != null && enemy.type.contains("KoopaTroopa")) {
                int col = enemy.direction ? 4 : 1;
                enemyImage = loader.getSubImage(spriteSheet, col, 3, 48, 64);
            }
            
            if (enemyImage != null) {
                g2.drawImage(enemyImage, enemy.x, enemy.y, null);
            } else {
                g2.setColor(Color.RED);
                g2.fillRect(enemy.x, enemy.y, 48, 48);
            }
        }
    }
    
    // 아이템들 렌더링 (GameStateMessage 기반)
    private void drawItemsFromState(Graphics2D g2, network.protocol.GameStateMessage gameState) {
        network.protocol.GameStateMessage.ItemState[] items = gameState.getItems();
        if (items == null) return;
        
        ImageLoader loader = engine.getImageLoader();
        
        for (network.protocol.GameStateMessage.ItemState item : items) {
            if (item == null || item.collected) continue;
            
            BufferedImage itemImage = loader.getSubImage(spriteSheet, 1, 5, 48, 48);
            
            if (itemImage != null) {
                g2.drawImage(itemImage, item.x, item.y, null);
            } else {
                g2.setColor(Color.YELLOW);
                g2.fillOval(item.x, item.y, 24, 24);
            }
        }
    }
    
    // 블록들 렌더링 (GameStateMessage 기반)
    private void drawBricksFromState(Graphics2D g2, network.protocol.GameStateMessage gameState) {
        network.protocol.GameStateMessage.BrickState[] bricks = gameState.getBricks();
        if (bricks == null) {
            return;
        }
        
        ImageLoader loader = engine.getImageLoader();
        
        for (network.protocol.GameStateMessage.BrickState brick : bricks) {
            if (brick == null) continue;
            
            BufferedImage brickImage = null;
            
            if ("OrdinaryBrick".equals(brick.type)) {
                brickImage = loader.getSubImage(spriteSheet, 1, 1, 48, 48);
            } else if ("SurpriseBrick".equals(brick.type)) {
                if (brick.empty) {
                    brickImage = loader.getSubImage(spriteSheet, 2, 3, 48, 48);
                } else {
                    brickImage = loader.getSubImage(spriteSheet, 2, 1, 48, 48);
                }
            } else if ("GroundBrick".equals(brick.type)) {
                brickImage = loader.getSubImage(spriteSheet, 2, 2, 48, 48);
            } else if ("Pipe".equals(brick.type)) {
                brickImage = loader.getSubImage(spriteSheet, 3, 1, 96, 96);
            }
            
            if (brickImage != null) {
                g2.drawImage(brickImage, brick.x, brick.y, null);
            }
        }
    }
    
    // HUD 렌더링: 점수, 생명, 코인, 시간, 리더보드
    private void drawHUDFromState(Graphics2D g2, network.protocol.GameStateMessage.PlayerState player,
                                   network.protocol.GameStateMessage gameState) {
        g2.setFont(gameFont.deriveFont(25f));
        g2.setColor(Color.WHITE);
        
        g2.drawString("POINTS: " + player.points, 300, 50);
        
        if (heartIcon != null) {
            g2.drawImage(heartIcon, 40, 30, 32, 32, null);
        }
        g2.drawString("x " + player.lives, 80, 55);
        
        if (coinIcon != null) {
            g2.drawImage(coinIcon, 40, 70, 24, 24, null);
        }
        g2.drawString("x " + player.coins, 80, 90);
        
        network.protocol.GameStateMessage.GameInfo info = gameState.getGameInfo();
        if (info != null) {
            g2.drawString("TIME: " + info.remainingTime, 750, 50);
        }
        
        drawLeaderboard(g2, gameState);
    }
    
    // 실시간 리더보드 렌더링 (우측 상단)
    private void drawLeaderboard(Graphics2D g2, network.protocol.GameStateMessage gameState) {
        if (gameState == null) return;
        
        class PlayerEntry {
            int id;
            network.protocol.GameStateMessage.PlayerState state;
            PlayerEntry(int id, network.protocol.GameStateMessage.PlayerState state) {
                this.id = id;
                this.state = state;
            }
        }
        
        java.util.List<PlayerEntry> players = new java.util.ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            network.protocol.GameStateMessage.PlayerState player = gameState.getPlayer(i);
            if (player != null && player.lives > 0) {
                players.add(new PlayerEntry(i, player));
            }
        }
        
        if (players.isEmpty()) return;
        
        players.sort((a, b) -> Integer.compare(b.state.points, a.state.points));
        
        int x = 950;
        int y = 100;
        int lineHeight = 30;
        int padding = 15;
        int boxWidth = 280;
        int boxHeight = players.size() * lineHeight + padding * 2 + 10;
        
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(x - padding, y - padding, boxWidth, boxHeight, 10, 10);
        
        g2.setColor(new Color(255, 215, 0));
        g2.setFont(gameFont.deriveFont(Font.BOLD, 20f));
        g2.drawString("LEADERBOARD", x, y);
        y += 35;
        
        g2.setFont(gameFont.deriveFont(18f));
        for (int i = 0; i < players.size(); i++) {
            PlayerEntry entry = players.get(i);
            
            if (i == 0) {
                g2.setColor(new Color(255, 215, 0));
            } else if (i == 1) {
                g2.setColor(new Color(192, 192, 192));
            } else if (i == 2) {
                g2.setColor(new Color(205, 127, 50));
            } else {
                g2.setColor(Color.WHITE);
            }
            
            String text = String.format("%d. P%d: %d pts", i + 1, entry.id, entry.state.points);
            g2.drawString(text, x, y);
            y += lineHeight;
        }
    }
}
