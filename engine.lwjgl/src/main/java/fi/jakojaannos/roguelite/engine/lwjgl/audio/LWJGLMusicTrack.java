package fi.jakojaannos.roguelite.engine.lwjgl.audio;

import org.lwjgl.stb.STBVorbisInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import fi.jakojaannos.roguelite.engine.view.audio.MusicTrack;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

public class LWJGLMusicTrack implements MusicTrack {
    @SuppressWarnings("FieldCanBeLocal")
    private final ByteBuffer encodedAudio;

    private final long handle;

    private final int channels;
    private final int sampleRate;
    private final int format;

    private final AtomicInteger sampleIndex = new AtomicInteger();

    @Override
    public int getFormat() {
        return this.format;
    }

    @Override
    public int getSampleRate() {
        return this.sampleRate;
    }

    public int getChannelCount() {
        return this.channels;
    }

    public LWJGLMusicTrack(final Path path) {
        try {
            final var bytes = Files.readAllBytes(path);
            this.encodedAudio = memAlloc(bytes.length);
            this.encodedAudio.put(0, bytes);
        } catch (final IOException e) {
            throw new IllegalStateException("Error loading track \"" + path + "\": " + e.getMessage());
        }

        try (final var stack = stackPush()) {
            final var error = stack.mallocInt(1);
            this.handle = stb_vorbis_open_memory(this.encodedAudio, error, null);
            if (this.handle == NULL) {
                throw new IllegalStateException("Opening .ogg file failed with error code " + error.get(0));
            }

            final var info = STBVorbisInfo.mallocStack(stack);
            stb_vorbis_get_info(this.handle, info);
            this.channels = info.channels();
            this.sampleRate = info.sample_rate();
        }

        this.format = switch (this.channels) {
            case 1 -> AL_FORMAT_MONO16;
            case 2 -> AL_FORMAT_STEREO16;
            default -> throw new IllegalStateException("Unsupported number of channels: " + this.channels);
        };

        this.sampleIndex.set(0);
    }

    @Override
    public void close() {
        stb_vorbis_close(this.handle);
        memFree(this.encodedAudio);
    }

    @Override
    public /* synchronized (?) */ void progressBy(final int samples) {
        this.sampleIndex.addAndGet(samples);
    }

    @Override
    public synchronized void rewind() {
        stb_vorbis_seek(this.handle, 0);
        this.sampleIndex.set(0);
    }

    synchronized int getSamples(final ShortBuffer pcm) {
        return stb_vorbis_get_samples_short_interleaved(this.handle,
                                                        this.channels,
                                                        pcm);
    }
}
