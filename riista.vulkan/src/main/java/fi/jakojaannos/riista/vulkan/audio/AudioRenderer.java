package fi.jakojaannos.riista.vulkan.audio;

import java.nio.ShortBuffer;
import javax.annotation.Nullable;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.AL_SAMPLE_OFFSET;
import static org.lwjgl.openal.EXTThreadLocalContext.alcSetThreadContext;
import static org.lwjgl.openal.SOFTDirectChannels.AL_DIRECT_CHANNELS_SOFT;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAllocShort;

public class AudioRenderer implements AutoCloseable {
    private static final int BUFFER_SIZE = 1024 * 8;
    private static final int BUFFER_COUNT = 2;

    private final ShortBuffer pcm;
    private final int source;
    private final int[] buffers;

    private int bufferOffset;
    private int lastOffset;
    private int offset;

    public AudioRenderer(final LWJGLAudioContext context) {
        this.pcm = memAllocShort(BUFFER_SIZE);

        alcSetThreadContext(context.getALContext());
        this.source = context.getMusicSource();
        alSourcei(this.source, AL_DIRECT_CHANNELS_SOFT, AL_TRUE);
        alSourcef(this.source, AL_GAIN, 0.25f);

        this.buffers = new int[BUFFER_COUNT];
        alGenBuffers(this.buffers);
    }

    @Override
    public void close() {
        alDeleteBuffers(this.buffers);
        alcSetThreadContext(NULL);
    }

    public UpdateResult update(
            final LWJGLMusicTrack track,
            @Nullable final LWJGLMusicTrack nextTrack,
            final boolean loopOrSeamless
    ) {
        // Get number of processed buffers
        final var processed = alGetSourcei(this.source, AL_BUFFERS_PROCESSED);

        var result = UpdateResult.OK;
        var activeTrack = track;
        for (var i = 0; i < processed; ++i) {
            this.bufferOffset += BUFFER_SIZE / activeTrack.getChannelCount();
            final var buffer = alSourceUnqueueBuffers(this.source);

            if (stream(activeTrack, buffer) == 0) {
                var shouldExit = true;

                if (loopOrSeamless) {
                    // Swap the tracks
                    if (nextTrack != null) {
                        activeTrack = nextTrack;
                        result = UpdateResult.SWAP;
                    }

                    activeTrack.rewind();
                    this.lastOffset = 0;
                    this.offset = 0;
                    this.bufferOffset = 0;

                    shouldExit = stream(activeTrack, buffer) == 0;
                }

                if (shouldExit) {
                    return UpdateResult.ERROR;
                }
            }
            alSourceQueueBuffers(this.source, buffer);
        }

        if (processed == 2) {
            alSourcePlay(this.source);
        }

        return result;
    }

    /**
     * Streams audio from the given track to the buffer with given index. This audio data can then be later queued for
     * playback using {@link org.lwjgl.openal.AL10#alSourceQueueBuffers(int, int[]) alQueueBuffers}
     *
     * @param track  track to stream
     * @param buffer buffer to store the data to
     *
     * @return the number of samples streamed to the buffer
     */
    private int stream(final LWJGLMusicTrack track, final int buffer) {
        int samples = 0;
        while (samples < BUFFER_SIZE) {
            this.pcm.position(samples);
            final var samplesPerChannel = track.getSamples(this.pcm);
            if (samplesPerChannel == 0) {
                break;
            }

            samples += samplesPerChannel * track.getChannelCount();
        }

        if (samples != 0) {
            this.pcm.position(0);
            this.pcm.limit(samples);
            alBufferData(buffer, track.getFormat(), this.pcm, track.getSampleRate());
            this.pcm.limit(BUFFER_SIZE);
        }

        return samples;
    }

    private void update(final LWJGLMusicTrack track) {
        final var sampleOffset = alGetSourcei(this.source, AL_SAMPLE_OFFSET);
        this.offset = this.bufferOffset + sampleOffset;
        track.progressBy(this.offset - this.lastOffset);
        this.lastOffset = this.offset;
    }

    private boolean play(final LWJGLMusicTrack track) {
        for (final var buffer : this.buffers) {
            if (stream(track, buffer) == 0) {
                return false;
            }
        }

        alSourceQueueBuffers(this.source, this.buffers);
        alSourcePlay(this.source);

        return true;
    }

    public AudioStateUpdater createStateUpdater() {
        return new AudioStateUpdater() {
            @Override
            public void update(final LWJGLMusicTrack track) {
                AudioRenderer.this.update(track);
            }

            @Override
            public boolean play(final LWJGLMusicTrack track) {
                return AudioRenderer.this.play(track);
            }
        };
    }

    enum UpdateResult {
        OK,
        ERROR,
        SWAP
    }
}
