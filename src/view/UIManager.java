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

/**
 * 게임의 모든 UI 요소를 관리하는 UI 관리자 클래스
 * JPanel을 상속받아 게임 화면을 그리는 주체
 * 시작 화면, 게임 플레이, 일시정지, 게임 오버 등 모든 화면 상태를 처리
 *
 * @author 네트워크프로그래밍 팀
 * @version 1.0
 * @since 2024-12-14
 */
public class UIManager extends JPanel{

    /**
     * 게임 엔진 참조 - 게임 상태와 데이터를 얻기 위해 사용
     */
    private GameEngine engine;
    
    /**
     * 게임 전용 폰트 - 마리오 스타일의 커스텀 폰트
     */
    private Font gameFont;
    
    /**
     * 각 화면 상태에 대한 배경 이미지들
     */
    private BufferedImage startScreenImage, aboutScreenImage, helpScreenImage, gameOverScreen;
    
    /**
     * UI 아이콘 이미지들
     */
    private BufferedImage heartIcon;      // 생명 아이콘
    private BufferedImage coinIcon;       // 코인 아이콘
    private BufferedImage selectIcon;     // 선택 아이콘
    
    /**
     * 스프라이트 시트 이미지
     */
    private BufferedImage spriteSheet;
    
    /**
     * 클라이언트 렌더링용 맵 배경 이미지
     */
    private BufferedImage mapBackgroundImage;
    private String currentMapName = null;
    
    /**
     * 맵 선택 화면을 관리하는 객체
     */
    private MapSelection mapSelection;
    
    /**
     * 플레이어별 애니메이션 객체 캐시
     */
    private java.util.Map<Integer, Animation> playerAnimations;
    

    /**
     * UI 관리자 생성자
     * 패널 크기 설정과 모든 리소스 로드를 초기화
     *
     * @param engine 게임 엔진 참조
     * @param width 패널 너비
     * @param height 패널 높이
     */
    public UIManager(GameEngine engine, int width, int height) {
        // 패널 크기 설정 (고정 크기)
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));

        this.engine = engine;
        ImageLoader loader = engine.getImageLoader();

        // 맵 선택 화면 초기화
        mapSelection = new MapSelection();
        
        // 애니메이션 캐시 초기화
        playerAnimations = new java.util.HashMap<>();

        // 스프라이트 시트 로드
        this.spriteSheet = loader.loadImage("/sprite.png");
        
        // UI 아이콘 이미지들 로드
        this.heartIcon = loader.loadImage("/heart-icon.png");
        this.coinIcon = loader.getSubImage(spriteSheet, 1, 5, 48, 48);
        this.selectIcon = loader.loadImage("/select-icon.png");
        
        // 각 화면 상태의 배경 이미지 로드
        this.startScreenImage = loader.loadImage("/start-screen.png");
        this.helpScreenImage = loader.loadImage("/help-screen.png");
        this.aboutScreenImage = loader.loadImage("/about-screen.png");
        this.gameOverScreen = loader.loadImage("/game-over.png");

        // 마리오 전용 폰트 로드 (TTF 파일)
        try {
            InputStream in = getClass().getResourceAsStream("/media/font/mario-font.ttf");
            gameFont = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (FontFormatException | IOException e) {
            // 폰트 로드 실패 시 기본 폰트 사용
            gameFont = new Font("Verdana", Font.PLAIN, 12);
            e.printStackTrace();
        }
    }

    /**
     * 패널을 그리는 핵심 메서드
     * 게임 상태에 따라 다른 화면을 렌더링
     *
     * @param g 그래픽스 컨텍스트 객체
     */
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        GameStatus gameStatus = engine.getGameStatus();

        // 게임 상태에 따른 화면 렌더링
        if(gameStatus == GameStatus.START_SCREEN){
            drawStartScreen(g2);                    // 시작 화면
        }
        else if(gameStatus == GameStatus.MAP_SELECTION){
            drawMapSelectionScreen(g2);              // 맵 선택 화면
        }
        else if(gameStatus == GameStatus.ABOUT_SCREEN){
            drawAboutScreen(g2);                    // 정보 화면
        }
        else if(gameStatus == GameStatus.HELP_SCREEN){
            drawHelpScreen(g2);                     // 도움말 화면
        }
        else if(gameStatus == GameStatus.GAME_OVER){
            drawGameOverScreen(g2);                  // 게임 오버 화면
        }
        else if(gameStatus == GameStatus.RANKING_SCREEN){
            drawRankingScreen(g2);                  // 랭킹 화면
        }
        else if(gameStatus == GameStatus.STAGE_SELECTION){
            drawStageSelectionScreen(g2);          // 스테이지 선택 화면
        }
        else if(gameStatus == GameStatus.WAITING_FOR_PLAYERS){
            drawWaitingForPlayersScreen(g2);       // 서버 대기 화면
        }
        else if(gameStatus == GameStatus.CONNECTING_TO_SERVER){
            drawConnectingScreen(g2);               // 클라이언트 접속 화면
        }
        else {
            // 게임 플레이 화면
            System.out.println("[UI] Drawing game screen");
            if(engine.getNetworkManager() != null && 
               engine.getNetworkManager().getNetworkMode() == network.NetworkManager.NetworkMode.CLIENT) {
                System.out.println("[UI] CLIENT MODE - calling drawGameFromState()");
                // 클라이언트 모드: 서버에서 받은 GameStateMessage 기반 렌더링
                drawGameFromState(g2);
            } else {
                System.out.println("[UI] SINGLE/SERVER MODE - drawing traditional way");
                // 싱글플레이어/서버 모드: 기존 방식
                Point camLocation = engine.getCameraLocation();
                g2.translate(-camLocation.x, -camLocation.y);  // 카메라 위치 조정
                engine.drawMap(g2);                         // 맵과 게임 오브젝트 렌더링
                
                // 다른 플레이어들 렌더링 (네트워크 모드)
                drawOtherPlayers(g2);
                
                g2.translate(camLocation.x, camLocation.y);      // 카메라 위치 복원

                // HUD 요소들 렌더링
                drawPoints(g2);                           // 점수 표시
                drawRemainingLives(g2);                    // 생명 표시
                drawAcquiredCoins(g2);                     // 코인 표시
                drawRemainingTime(g2);                     // 시간 표시

                // 게임 중 특수 상태 화면
                if(gameStatus == GameStatus.PAUSED){
                    drawPauseScreen(g2);                   // 일시정지 화면
                }
                else if(gameStatus == GameStatus.MISSION_PASSED){
                    drawVictoryScreen(g2);                 // 승리 화면
                }
            }
        }

        g2.dispose();  // 그래픽스 리소스 해제
    }

    /**
     * 랭킹 화면을 그리는 메서드
     * 점수 순위와 시간 순위를 시각적으로 표시
     *
     * @param g2 그래픽스 컨텍스트 객체
     */
    private void drawRankingScreen(Graphics2D g2) {
        // 배경
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 제목
        g2.setColor(Color.YELLOW);
        g2.setFont(gameFont.deriveFont(40f));
        String title = "🏆 랭킹 시스템 🏆";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (getWidth() - titleWidth) / 2, 50);

        // 랭킹 관리자 가져오기
        RankingManager rankingManager = new RankingManager();

        // 점수 순위 표시
        drawRankingList(g2, "🥇 점수 순위", rankingManager.getScoreRanking(), 150, new Color(255, 215, 0));

        // 시간 순위 표시
        drawRankingList(g2, "⏱️ 시간 순위", rankingManager.getTimeRanking(), 400, Color.CYAN);

        // 돌아가기 안내
        g2.setColor(Color.WHITE);
        g2.setFont(gameFont.deriveFont(20f));
        String backMessage = "SPACE 키를 누르면 메뉴로 돌아갑니다";
        int backWidth = g2.getFontMetrics().stringWidth(backMessage);
        g2.drawString(backMessage, (getWidth() - backWidth) / 2, getHeight() - 50);
    }

    /**
     * 순위 리스트를 그리는 보조 메서드
     *
     * @param g2 그래픽스 컨텍스트
     * @param title 순위 제목
     * @param ranking 순위 리스트
     * @param startY 시작 Y 위치
     * @param titleColor 제목 색상
     */
    private void drawRankingList(Graphics2D g2, String title, java.util.List<GameRecord> ranking, int startY, Color titleColor) {
        // 제목
        g2.setColor(titleColor);
        g2.setFont(gameFont.deriveFont(25f));
        g2.drawString(title, 100, startY);

        // 순위 표시
        g2.setColor(Color.WHITE);
        g2.setFont(gameFont.deriveFont(18f));

        for (int i = 0; i < Math.min(ranking.size(), 10); i++) {
            GameRecord record = ranking.get(i);
            int y = startY + 40 + (i * 25);

            // 순위 아이콘
            String rankIcon = getRankIcon(i);
            g2.drawString(rankIcon, 120, y);

            // 플레이어 정보
            String playerInfo = String.format("%s - %d점 (%s)", 
                record.getPlayerName(), 
                record.getScore(), 
                record.getFormattedTime());
            g2.drawString(playerInfo, 160, y);
        }

        // 순위가 없을 경우
        if (ranking.isEmpty()) {
            g2.drawString("아직 기록이 없습니다", 120, startY + 40);
        }
    }

    /**
     * 순위에 따른 아이콘을 반환하는 메서드
     *
     * @param rank 순위 (0부터 시작)
     * @return 순위 아이콘 문자열
     */
    private String getRankIcon(int rank) {
        switch (rank) {
            case 0: return "🥇";
            case 1: return "🥈";
            case 2: return "🥉";
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
        int stringLength = g2.getFontMetrics().stringWidth(displayedStr);
        //g2.drawImage(coinIcon, 50, 10, null);
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
    
    /**
     * 서버 대기 화면을 그리는 메서드
     * 클라이언트 접속을 대기하는 화면
     *
     * @param g2 그래픽스 컨텍스트 객체
     */
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
    
    /**
     * 스테이지 선택 화면을 그리는 메서드
     * Stage 1 또는 Stage 2를 선택
     *
     * @param g2 그래픽스 컨텍스트 객체
     */
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
    
    /**
     * 클라이언트 접속 화면을 그리는 메서드
     * 서버 IP 주소 입력 안내
     *
     * @param g2 그래픽스 컨텍스트 객체
     */
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
    
    /**
     * 다른 플레이어들을 렌더링하는 메서드 (네트워크 멀티플레이어)
     * 자신이 아닌 다른 플레이어들을 반투명하게 그림
     *
     * @param g2 그래픽스 컨텍스트 (카메라 좌표계)
     */
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
    
    /**
     * 플레이어 위에 라벨을 표시하는 메서드
     *
     * @param g2 그래픽스 컨텍스트
     * @param player 플레이어 객체
     * @param label 표시할 라벨
     */
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
    
    /**
     * 서버로부터 받은 GameStateMessage 기반으로 게임 화면을 렌더링
     *
     * @param g2 그래픽스 컨텍스트
     */
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
        
        int myPlayerId = engine.getNetworkManager().getCurrentPlayerId();
        network.protocol.GameStateMessage.PlayerState myPlayer = gameState.getPlayer(myPlayerId);
        
        if (myPlayer == null) {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
            return;
        }
        
        int cameraX = myPlayer.x - getWidth() / 2;
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
    
    /**
     * GameStateMessage의 플레이어 정보를 기반으로 플레이어들을 렌더링
     *
     * @param g2 그래픽스 컨텍스트
     * @param gameState 게임 상태 메시지
     * @param myPlayerId 내 플레이어 ID
     */
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
    
    /**
     * GameStateMessage의 적 정보를 기반으로 적들을 렌더링
     *
     * @param g2 그래픽스 컨텍스트
     * @param gameState 게임 상태 메시지
     */
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
    
    /**
     * GameStateMessage의 아이템 정보를 기반으로 아이템들을 렌더링
     *
     * @param g2 그래픽스 컨텍스트
     * @param gameState 게임 상태 메시지
     */
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
    
    /**
     * GameStateMessage 기반 블록 렌더링
     *
     * @param g2 그래픽스 컨텍스트
     * @param gameState 게임 상태
     */
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
    
    /**
     * GameStateMessage 기반 HUD 렌더링
     *
     * @param g2 그래픽스 컨텍스트
     * @param player 플레이어 상태
     * @param gameState 게임 상태
     */
    private void drawHUDFromState(Graphics2D g2, network.protocol.GameStateMessage.PlayerState player,
                                   network.protocol.GameStateMessage gameState) {
        g2.setFont(gameFont.deriveFont(25f));
        g2.setColor(Color.WHITE);
        
        // 점수 표시 (중앙 좌측)
        g2.drawString("POINTS: " + player.points, 300, 50);
        
        // 목숨 표시 (좌측 상단)
        if (heartIcon != null) {
            g2.drawImage(heartIcon, 40, 30, 32, 32, null);
        }
        g2.drawString("x " + player.lives, 80, 55);
        
        // 코인 표시 (목숨 아래)
        if (coinIcon != null) {
            g2.drawImage(coinIcon, 40, 70, 24, 24, null);
        }
        g2.drawString("x " + player.coins, 80, 90);
        
        // 시간 표시 (우측 상단)
        network.protocol.GameStateMessage.GameInfo info = gameState.getGameInfo();
        if (info != null) {
            g2.drawString("TIME: " + info.remainingTime, 750, 50);
        }
    }
}