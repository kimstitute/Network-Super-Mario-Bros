package model.prize;

import manager.GameEngine;
import manager.MapManager;
import model.hero.Mario;
import java.awt.*;

// 아이템 인터페이스: 코인, 버섯, 파이어플라워 등이 구현
public interface Prize {

    int getPoint();

    void reveal();

    Rectangle getBounds();

    void onTouch(Mario mario, GameEngine engine);

}
