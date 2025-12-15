package manager;

// 게임 상태 정의: 각 상태에 따라 다른 화면과 입력 처리 로직 실행
public enum GameStatus {
    GAME_OVER,              // 생명 소진 또는 시간 초과
    PAUSED,                 // 일시정지
    RUNNING,                // 게임 플레이 중
    START_SCREEN,           // 메인 메뉴
    MAP_SELECTION,          // 맵 선택 화면
    HELP_SCREEN,            // 조작 방법 안내
    MISSION_PASSED,         // 스테이지 클리어
    ABOUT_SCREEN,           // 게임 정보 화면
    RANKING_SCREEN,         // 점수 랭킹 화면
    STAGE_SELECTION,        // 스테이지 선택 화면 (방 생성 시)
    WAITING_FOR_PLAYERS,    // 방에서 플레이어 대기 중
    CONNECTING_TO_SERVER    // 서버 접속 중
}
