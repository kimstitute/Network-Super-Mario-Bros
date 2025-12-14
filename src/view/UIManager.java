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
     * 맵 선택 화면을 관리하는 객체
     */
    private MapSelection mapSelection;

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

        // 스프라이트 시트 로드
        BufferedImage sprite = loader.loadImage("/sprite.png");
        
        // UI 아이콘 이미지들 로드
        this.heartIcon = loader.loadImage("/heart-icon.png");
        this.coinIcon = loader.getSubImage(sprite, 1, 5, 48, 48);
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
        else if(gameStatus == GameStatus.WAITING_FOR_PLAYERS){
            drawWaitingForPlayersScreen(g2);       // 서버 대기 화면
        }
        else if(gameStatus == GameStatus.CONNECTING_TO_SERVER){
            drawConnectingScreen(g2);               // 클라이언트 접속 화면
        }
        else {
            // 게임 플레이 화면
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
        g2.drawImage(helpScreenImage, 0, 0, null);
    }

    private void drawAboutScreen(Graphics2D g2) {
        g2.drawImage(aboutScreenImage, 0, 0, null);
    }

    private void drawGameOverScreen(Graphics2D g2) {
        g2.drawImage(gameOverScreen, 0, 0, null);
        g2.setFont(gameFont.deriveFont(50f));
        g2.setColor(new Color(130, 48, 48));
        String acquiredPoints = "Score: " + engine.getScore();
        int stringLength = g2.getFontMetrics().stringWidth(acquiredPoints);
        int stringHeight = g2.getFontMetrics().getHeight();
        g2.drawString(acquiredPoints, (getWidth()-stringLength)/2, getHeight()-stringHeight*2);
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
        
        g2.setFont(gameFont.deriveFont(30f));
        g2.setColor(Color.WHITE);
        
        String[] menuItems = {
            "SINGLE PLAYER",
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
        return getMousePosition();
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
        int playerCount = 1;
        
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
}