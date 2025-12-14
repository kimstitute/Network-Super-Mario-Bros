package manager;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * 게임의 모든 사운드 효과와 배경음악을 관리하는 사운드 관리자 클래스
 * WAV 형식의 오디오 파일을 로드하여 재생하고, 배경음악의 일시정지/재개 기능 제공
 * Java Sound API를 사용하여 오디오 스트림을 제어
 *
 * @author 네트워크프로그래밍 팀
 * @version 1.0
 * @since 2024-12-14
 */
public class SoundManager {

    /**
     * 배경음악을 재생하는 Clip 객체
     * 게임 진행 중에 계속 재생되며, 일시정지/재개 기능 지원
     */
    private Clip background;
    
    /**
     * 배경음악의 현재 재생 위치를 저장 (마이크로초 단위)
     * 일시정지 시 재생 위치를 기억했다가 재개 시 복원하는 데 사용
     */
    private long clipTime = 0;

    /**
     * 사운드 관리자 생성자
     * 배경음악 파일을 로드하여 초기화
     */
    public SoundManager() {
        background = getClip(loadAudio("background"));  // 배경음악 로드 및 초기화
    }

    /**
     * 지정된 이름의 오디오 파일을 로드하여 AudioInputStream으로 반환
     * /media/audio/ 폴더에서 .wav 파일을 찾아 로드
     *
     * @param url 로드할 오디오 파일의 이름 (확장자 제외)
     * @return 오디오 스트림 객체, 로드 실패 시 null 반환
     */
    private AudioInputStream loadAudio(String url) {
        try {
            // 클래스패스에서 오디오 파일을 리소스로 로드
            InputStream audioSrc = getClass().getResourceAsStream("/media/audio/" + url + ".wav");
            // 버퍼링을 통해 성능 향상
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            // 오디오 시스템에서 지원하는 형식으로 변환
            return AudioSystem.getAudioInputStream(bufferedIn);

        } catch (Exception e) {
            // 오디오 로드 실패 시 에러 메시지 출력
            System.err.println(e.getMessage());
        }

        return null;  // 실패 시 null 반환
    }

    /**
     * AudioInputStream으로부터 Clip 객체를 생성
     * Clip은 오디오를 재생하고 제어하는 데 사용되는 Java Sound API 핵심 클래스
     *
     * @param stream 오디오 데이터 스트림
     * @return 생성된 Clip 객체, 실패 시 null 반환
     */
    private Clip getClip(AudioInputStream stream) {
        try {
            Clip clip = AudioSystem.getClip();  // 새로운 Clip 객체 생성
            clip.open(stream);                 // 오디오 스트림을 Clip에 로드
            return clip;
        } catch (Exception e) {
            // Clip 생성 실패 시 스택 트레이스 출력
            e.printStackTrace();
        }

        return null;  // 실패 시 null 반환
    }

    /**
     * 일시정지되었던 배경음악을 재개
     * 이전에 저장된 재생 위치부터 다시 시작
     */
    public void resumeBackground(){
        background.setMicrosecondPosition(clipTime);  // 저장된 위치로 이동
        background.start();                        // 재생 시작
    }

    /**
     * 현재 재생 중인 배경음악을 일시정지
     * 현재 재생 위치를 저장하여 나중에 재개할 수 있도록 함
     */
    public void pauseBackground(){
        clipTime = background.getMicrosecondPosition();  // 현재 위치 저장
        background.stop();                            // 재생 중지
    }

    /**
     * 배경음악을 처음부터 다시 시작
     * 재생 위치를 초기화하고 재개
     */
    public void restartBackground() {
        clipTime = 0;          // 재생 위치 초기화
        resumeBackground();       // 재생 시작
    }

    /**
     * 점프 사운드 효과 재생
     * 마리오가 점프할 때 호출됨
     */
    public void playJump() {
        Clip clip = getClip(loadAudio("jump"));  // 점프 사운드 로드
        clip.start();                           // 즉시 재생
    }

    /**
     * 코인 획득 사운드 효과 재생
     * 마리오가 코인을 획득했을 때 호출됨
     */
    public void playCoin() {
        Clip clip = getClip(loadAudio("coin"));  // 코인 사운드 로드
        clip.start();                           // 즉시 재생
    }

    /**
     * 파이어볼 발사 사운드 효과 재생
     * 마리오가 파이어볼을 발사했을 때 호출됨
     */
    public void playFireball() {
        Clip clip = getClip(loadAudio("fireball"));  // 파이어볼 사운드 로드
        clip.start();                              // 즉시 재생
    }

    /**
     * 게임 오버 사운드 효과 재생
     * 마리오의 생명이 모두 소진되었을 때 호출됨
     */
    public void playGameOver() {
        Clip clip = getClip(loadAudio("gameOver"));  // 게임 오버 사운드 로드
        clip.start();                               // 즉시 재생
    }

    /**
     * 적 밟기 사운드 효과 재생
     * 마리오가 적을 밟았을 때 호출됨
     */
    public void playStomp() {
        Clip clip = getClip(loadAudio("stomp"));  // 밟기 사운드 로드
        clip.start();                            // 즉시 재생
    }

    /**
     * 1업 아이템 획득 사운드 효과 재생
     * 마리오가 1업 버섯을 획득했을 때 호출됨
     */
    public void playOneUp() {
        Clip clip = getClip(loadAudio("oneUp"));  // 1업 사운드 로드
        clip.start();                            // 즉시 재생
    }

    /**
     * 슈퍼 버섯 획득 사운드 효과 재생
     * 마리오가 슈퍼 버섯을 획득했을 때 호출됨
     */
    public void playSuperMushroom() {
        Clip clip = getClip(loadAudio("superMushroom"));  // 슈퍼 버섯 사운드 로드
        clip.start();                                    // 즉시 재생
    }

    /**
     * 마리오 사망 사운드 효과 재생
     * 마리오가 적에게 공격당했을 때 호출됨
     */
    public void playMarioDies() {
        Clip clip = getClip(loadAudio("marioDies"));  // 마리오 사망 사운드 로드
        clip.start();                                 // 즉시 재생
    }

    /**
     * 파이어플라워 획득 사운드 효과 재생
     * 마리오가 파이어플라워를 획득했을 때 호출됨
     * 현재는 구현되어 있지 않음 (사운드 파일 없음)
     */
    public void playFireFlower() {
        // 파이어플라워 사운드 효과 (미구현 상태)
        // 필요시 사운드 파일 추가 후 구현 가능
    }

}
