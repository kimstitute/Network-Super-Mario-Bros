package manager;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

// 사운드 관리자: 배경음악과 효과음 재생
public class SoundManager {

    private Clip background; // 배경음악 Clip
    private long clipTime = 0; // 일시정지 시 재생 위치 저장 (마이크로초)

    public SoundManager() {
        background = getClip(loadAudio("background"));
    }

    // /media/audio/ 경로에서 WAV 파일 로드
    private AudioInputStream loadAudio(String url) {
        try {
            InputStream audioSrc = getClass().getResourceAsStream("/media/audio/" + url + ".wav");
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            return AudioSystem.getAudioInputStream(bufferedIn);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return null;
    }

    // AudioInputStream을 Clip으로 변환
    private Clip getClip(AudioInputStream stream) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // 배경음악 재개 (저장된 위치부터)
    public void resumeBackground(){
        background.setMicrosecondPosition(clipTime);
        background.start();
    }

    // 배경음악 일시정지 (현재 위치 저장)
    public void pauseBackground(){
        clipTime = background.getMicrosecondPosition();
        background.stop();
    }

    // 배경음악 처음부터 재시작
    public void restartBackground() {
        clipTime = 0;
        resumeBackground();
    }

    public void playJump() {
        Clip clip = getClip(loadAudio("jump"));
        clip.start();
    }

    public void playCoin() {
        Clip clip = getClip(loadAudio("coin"));
        clip.start();
    }

    public void playFireball() {
        Clip clip = getClip(loadAudio("fireball"));
        clip.start();
    }

    public void playGameOver() {
        Clip clip = getClip(loadAudio("gameOver"));
        clip.start();
    }

    public void playStomp() {
        Clip clip = getClip(loadAudio("stomp"));
        clip.start();
    }

    public void playOneUp() {
        Clip clip = getClip(loadAudio("oneUp"));
        clip.start();
    }

    public void playSuperMushroom() {
        Clip clip = getClip(loadAudio("superMushroom"));
        clip.start();
    }

    public void playMarioDies() {
        Clip clip = getClip(loadAudio("marioDies"));
        clip.start();
    }

    public void playFireFlower() {
        // 파이어플라워 효과음 (미구현)
    }

}
