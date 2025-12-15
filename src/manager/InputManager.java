package manager;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

// 입력 관리자: 키보드/마우스 입력을 ButtonAction으로 변환하여 GameEngine에 전달
public class InputManager implements KeyListener, MouseListener{

    private GameEngine engine;

    InputManager(GameEngine engine) {
        this.engine = engine; 
    }

    @Override
    public void keyPressed(KeyEvent event) {
        int keyCode = event.getKeyCode();
        GameStatus status = engine.getGameStatus();
        ButtonAction currentAction = ButtonAction.NO_ACTION;

        // 게임 오버 시: 1초 후 종료 허용
        if (status == GameStatus.GAME_OVER) {
            long timeSinceGameOver = System.currentTimeMillis() - engine.getGameOverTime();
            if (timeSinceGameOver > 1000) {
                System.out.println("[INPUT] Game over screen displayed for " + timeSinceGameOver + "ms, exiting...");
                System.exit(0);
            }
            return;
        }

        // 키 입력을 게임 상태에 따라 다른 액션으로 매핑
        if (keyCode == KeyEvent.VK_UP) {
            if(status == GameStatus.START_SCREEN || status == GameStatus.STAGE_SELECTION)
                currentAction = ButtonAction.GO_UP;
            else
                currentAction = ButtonAction.JUMP;
        }
        else if(keyCode == KeyEvent.VK_DOWN){
            if(status == GameStatus.START_SCREEN || status == GameStatus.STAGE_SELECTION)
                currentAction = ButtonAction.GO_DOWN;
        }
        else if (keyCode == KeyEvent.VK_RIGHT) {
            currentAction = ButtonAction.M_RIGHT;
        }
        else if (keyCode == KeyEvent.VK_LEFT) {
            currentAction = ButtonAction.M_LEFT;
        }
        else if (keyCode == KeyEvent.VK_ENTER) {
            currentAction = ButtonAction.SELECT;
        }
        else if (keyCode == KeyEvent.VK_ESCAPE) {
            if(status == GameStatus.RUNNING || status == GameStatus.PAUSED )
                currentAction = ButtonAction.PAUSE_RESUME;
            else
                currentAction = ButtonAction.GO_TO_START_SCREEN;
        }
        else if (keyCode == KeyEvent.VK_SPACE){
            currentAction = ButtonAction.FIRE;
        }

        notifyInput(currentAction);
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent event) {
        // 이동 키를 뗐을 때 마리오 속도를 0으로 설정
        if(event.getKeyCode() == KeyEvent.VK_RIGHT || event.getKeyCode() == KeyEvent.VK_LEFT)
            notifyInput(ButtonAction.ACTION_COMPLETED);
    }

    // 액션을 게임 엔진에 전달
    private void notifyInput(ButtonAction action) {
        if(action != ButtonAction.NO_ACTION)
            engine.receiveInput(action);
    }

    @Override
    public void keyTyped(KeyEvent arg0) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}
