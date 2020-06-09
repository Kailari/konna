package fi.jakojaannos.riista.vulkan.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;

import fi.jakojaannos.riista.view.audio.MusicPlayer;
import fi.jakojaannos.riista.view.assets.MusicTrack;

public class LWJGLMusicPlayer implements MusicPlayer {
    private static final Logger LOG = LoggerFactory.getLogger(LWJGLMusicPlayer.class);

    private final AtomicBoolean paused = new AtomicBoolean(true);
    private final Thread audioThread;

    private boolean audioThreadRunning;
    private AudioStateUpdater stateUpdater;

    @Nullable
    private LWJGLMusicTrack nextTrack;
    @Nullable
    private LWJGLMusicTrack playingTrack;

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
                        final var updateResult = audioRenderer.update(this.playingTrack, this.nextTrack, true);
                        if (updateResult == AudioRenderer.UpdateResult.ERROR) {
                            LOG.error("Audio playback failed");
                            this.paused.set(true);
                        } else if (updateResult == AudioRenderer.UpdateResult.SWAP) {
                            this.playingTrack = this.nextTrack;
                            this.nextTrack = null;
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
        this.nextTrack = (LWJGLMusicTrack) track;
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
