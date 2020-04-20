package fi.jakojaannos.roguelite.engine.lwjgl.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.view.audio.MusicPlayer;
import fi.jakojaannos.roguelite.engine.view.audio.MusicTrack;

public class LWJGLMusicPlayer implements MusicPlayer {
    private static final Logger LOG = LoggerFactory.getLogger(LWJGLMusicPlayer.class);

    private final AtomicBoolean paused = new AtomicBoolean(true);
    private final Thread audioThread;

    @Nullable
    private LWJGLMusicTrack playingTrack;
    private boolean audioThreadRunning;
    private AudioStateUpdater stateUpdater;

    public LWJGLMusicPlayer(final LWJGLAudioContext context) {
        LOG.debug("Creating music player!");
        final var audioLatch = new CountDownLatch(1);
        this.audioThread = new Thread(() -> {
            this.audioThreadRunning = true;

            try (final var audioRenderer = new AudioRenderer(context)) {
                this.stateUpdater = audioRenderer.createStateUpdater();

                audioLatch.countDown();
                while (this.audioThreadRunning) {
                    if (!this.paused.get()) {
                        if (!audioRenderer.update(this.playingTrack, true)) {
                            LOG.error("Audio playback failed");
                            this.paused.set(true);
                        }
                    }

                    try {
                        //noinspection BusyWait
                        Thread.sleep(1000 / 30);
                    } catch (final InterruptedException ignored) {
                    }
                }
            } catch (final Throwable t) {
                LOG.error("Audio thread has crashed: {}", t.getMessage());
                audioLatch.countDown();
            }

            this.audioThreadRunning = false;
        });
        this.audioThread.setName("konna-background-audio");
        this.audioThread.start();

        try {
            audioLatch.await();
        } catch (final InterruptedException ignored) {
        }
    }

    @Override
    public void playNow(final MusicTrack track) {
        this.playingTrack = (LWJGLMusicTrack) track;
        this.paused.set(false);
        this.stateUpdater.play((LWJGLMusicTrack) track);
    }

    @Override
    public void nextTrack(final MusicTrack track) {

    }

    @Override
    public void update() {
        this.stateUpdater.update(this.playingTrack);
    }

    @Override
    public void close() {
        LOG.debug("Closing music player!");
        this.audioThreadRunning = false;
        try {
            this.audioThread.join();
        } catch (final InterruptedException ignored) {
        }
    }
}
