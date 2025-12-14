package manager;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * 게임의 모든 입력을 처리하는 입력 관리자 클래스
 * 키보드와 마우스 입력을 감지하여 GameEngine에 전달하는 중개자 역할
 * 게임 상태에 따라 동일한 키가 다른 액션으로 매핑될 수 있음
 *
 * @author 네트워크프로그래밍 팀
 * @version 1.0
 * @since 2024-12-14
 */
public class InputManager implements KeyListener, MouseListener{

    /**
     * 입력을 전달받을 게임 엔진 참조
     * 모든 입력 이벤트를 최종적으로 처리하는 주체
     */
    private GameEngine engine;

    /**
     * 입력 관리자 생성자
     * 게임 엔진과의 연결을 초기화
     *
     * @param engine 입력을 전달받을 GameEngine 인스턴스
     */
    InputManager(GameEngine engine) {
        this.engine = engine; }

    /**
     * 키보드 키를 눌렀을 때 호출되는 메서드
     * 현재 게임 상태에 따라 키 입력을 다른 액션으로 매핑
     *
     * @param event 키보드 이벤트 정보를 담은 KeyEvent 객체
     */
    @Override
    public void keyPressed(KeyEvent event) {
        int keyCode = event.getKeyCode();                    // 눌린 키의 코드 값
        GameStatus status = engine.getGameStatus();           // 현재 게임 상태 확인
        ButtonAction currentAction = ButtonAction.NO_ACTION;   // 기본값은 무 액션

        // 위쪽 방향키 처리 - 게임 상태에 따라 다른 액션으로 매핑
        if (keyCode == KeyEvent.VK_UP) {
            if(status == GameStatus.START_SCREEN || status == GameStatus.MAP_SELECTION)
                currentAction = ButtonAction.GO_UP;      // 메뉴에서는 위쪽 이동
            else
                currentAction = ButtonAction.JUMP;        // 게임 중에는 점프
        }
        // 아래쪽 방향키 처리 - 메뉴에서만 사용
        else if(keyCode == KeyEvent.VK_DOWN){
            if(status == GameStatus.START_SCREEN || status == GameStatus.MAP_SELECTION)
                currentAction = ButtonAction.GO_DOWN;    // 메뉴에서 아래쪽 이동
        }
        // 오른쪽 방향키 처리 - 게임 중 이동
        else if (keyCode == KeyEvent.VK_RIGHT) {
            currentAction = ButtonAction.M_RIGHT;       // 마리오 오른쪽 이동
        }
        // 왼쪽 방향키 처리 - 게임 중 이동
        else if (keyCode == KeyEvent.VK_LEFT) {
            currentAction = ButtonAction.M_LEFT;        // 마리오 왼쪽 이동
        }
        // Enter키 처리 - 메뉴 선택 확인
        else if (keyCode == KeyEvent.VK_ENTER) {
            currentAction = ButtonAction.SELECT;         // 메뉴 항목 선택
        }
        // ESC키 처리 - 일시정지/메뉴 복귀
        else if (keyCode == KeyEvent.VK_ESCAPE) {
            if(status == GameStatus.RUNNING || status == GameStatus.PAUSED )
                currentAction = ButtonAction.PAUSE_RESUME;   // 게임 일시정지/재개
            else
                currentAction = ButtonAction.GO_TO_START_SCREEN; // 시작 화면으로 복귀

        }
        // 스페이스바 처리 - 파이어볼 발사
        else if (keyCode == KeyEvent.VK_SPACE){
            currentAction = ButtonAction.FIRE;          // 파이어볼 발사
        }

        // 변환된 액션을 게임 엔진에 전달
        notifyInput(currentAction);
    }

    /**
     * 마우스 버튼을 눌렀을 때 호출되는 메서드
     * 현재는 맵 선택 화면에서만 사용됨
     *
     * @param e 마우스 이벤트 정보를 담은 MouseEvent 객체
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if(engine.getGameStatus() == GameStatus.MAP_SELECTION){
            engine.selectMapViaMouse();                 // 마우스로 맵 선택
        }
    }

    /**
     * 키보드 키를 뗐을 때 호출되는 메서드
     * 주로 이동 키를 뗐을 때 수평 속도를 0으로 만드는 데 사용
     *
     * @param event 키보드 이벤트 정보를 담은 KeyEvent 객체
     */
    @Override
    public void keyReleased(KeyEvent event) {
        // 오른쪽이나 왼쪽 이동 키를 뗐을 때만 처리
        if(event.getKeyCode() == KeyEvent.VK_RIGHT || event.getKeyCode() == KeyEvent.VK_LEFT)
            notifyInput(ButtonAction.ACTION_COMPLETED);   // 이동 완료 액션 전달
    }

    /**
     * 변환된 액션을 게임 엔진에 전달하는 private 메서드
     * 무 액션이 아닐 경우에만 전달하여 불필요한 처리 방지
     *
     * @param action 전달할 ButtonAction 객체
     */
    private void notifyInput(ButtonAction action) {
        if(action != ButtonAction.NO_ACTION)
            engine.receiveInput(action);
    }

    // 아래는 인터페이스 구현을 위한 미사용 메서드들
    @Override
    public void keyTyped(KeyEvent arg0) {}              // 문자 입력 처리 (미사용)

    @Override
    public void mouseClicked(MouseEvent e) {}              // 마우스 클릭 (미사용)

    @Override
    public void mouseReleased(MouseEvent e) {}            // 마우스 버튼 릴리즈 (미사용)

    @Override
    public void mouseEntered(MouseEvent e) {}              // 마우스 컴포넌트 진입 (미사용)

    @Override
    public void mouseExited(MouseEvent e) {}               // 마우스 컴포넌트 이탈 (미사용)
}
