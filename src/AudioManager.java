package src;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

class AudioManager {
    private Clip bgmClip;
    private String currentMusicFile = "";
    private final GamePanel game;

    AudioManager(GamePanel game) {
        this.game = game;
    }

    void updateMapMusic() {
        if (game.mapIndex == 0) {
            playLoopingMapMusic("ED6101.wav");
        } else if (game.mapIndex == 1) {
            playLoopingMapMusic("ED6106.wav");
        } else {
            stopCurrentMusic();
        }
    }

    void playBattleMusic() {
        playLoopingMapMusic("ED6400.wav");
    }

    void playBattleEndMusic() {
        playOneshotMusic("battle end.wav");
    }

    void playOneshotMusic(String fileName) {
        stopCurrentMusic();

        File musicFile = new File("resources" + File.separator + fileName);
        System.out.println("嘗試播放結算音樂: " + fileName);
        System.out.println("檔案存在: " + musicFile.exists());
        System.out.println("檔案路徑: " + musicFile.getAbsolutePath());

        if (!musicFile.exists()) {
            System.err.println("找不到音樂檔案: " + fileName);
            return;
        }

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);
            bgmClip.start();
            currentMusicFile = fileName;
            System.out.println("結算音樂已開始播放");
        } catch (Exception e) {
            System.err.println("無法播放音樂檔案: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void playLoopingMapMusic(String fileName) {
        if (fileName.equals(currentMusicFile) && bgmClip != null && bgmClip.isRunning()) {
            return;
        }

        stopCurrentMusic();

        File musicFile = new File("resources" + File.separator + fileName);
        if (!musicFile.exists()) {
            System.err.println("找不到地圖音樂檔案: " + fileName);
            return;
        }

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
            currentMusicFile = fileName;
        } catch (Exception e) {
            System.err.println("無法播放音樂檔案: " + e.getMessage());
        }
    }

    void stopCurrentMusic() {
        if (bgmClip != null) {
            if (bgmClip.isRunning()) {
                bgmClip.stop();
            }
            bgmClip.close();
            bgmClip = null;
        }
        currentMusicFile = "";
    }

    void stopBackgroundMusic() {
        stopCurrentMusic();
    }
}
