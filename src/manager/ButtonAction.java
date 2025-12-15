package manager;

// 게임 입력 액션 정의: 키보드/마우스 입력을 추상화
public enum ButtonAction {
    JUMP,                   // 점프
    M_RIGHT,                // 오른쪽 이동
    M_LEFT,                 // 왼쪽 이동
    CROUCH,                 // 앉기
    FIRE,                   // 파이어볼 발사
    START,                  // 게임 시작
    PAUSE_RESUME,           // 일시정지/재개
    ACTION_COMPLETED,       // 키를 뗌
    SELECT,                 // 선택 확인
    GO_UP,                  // 메뉴 위로
    GO_DOWN,                // 메뉴 아래로
    GO_TO_START_SCREEN,     // 시작 화면으로
    NO_ACTION               // 액션 없음
}
