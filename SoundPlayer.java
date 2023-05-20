import javax.sound.sampled.*;
import java.io.File;

public class SoundPlayer {
    private Clip soundClip;
    private String filePath;    

    // -1 = infinite loop
    // num = num loops
    private int statusBeforePause = 0;

    public SoundPlayer(String pathToFile) {
        filePath = pathToFile;
        setup();
    }

    private void setup() {
        try {
            File f = new File(filePath);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
            soundClip = AudioSystem.getClip();
            soundClip.open(audioIn);
            audioIn.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void startLoop(int count) {
        statusBeforePause = count;
        soundClip.loop(count);
    }

    public void startLoop(boolean infinite) {
        statusBeforePause = infinite ? Clip.LOOP_CONTINUOUSLY : 1;
        soundClip.loop(infinite ? Clip.LOOP_CONTINUOUSLY : 1);
    }

    public void pause() {
        soundClip.stop();
    }

    public void resume() {
        if (statusBeforePause == Clip.LOOP_CONTINUOUSLY) {
            startLoop(true);
        } else {
            startLoop(statusBeforePause);
        }
    }

    public void close() {
        soundClip.close();
    }

    public void reset() {
        soundClip.setFramePosition(0);
    }

    public boolean isPlaying() {
        return soundClip.isRunning();
    }

    public void setLoopStartEnd(int start, int end) {
        soundClip.setLoopPoints(start, end);
    }
}
