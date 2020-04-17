package fi.jakojaannos.roguelite.engine.lwjgl.audio;

import java.nio.ShortBuffer;
import java.nio.file.Path;

import fi.jakojaannos.roguelite.engine.view.audio.AudioContext;
import fi.jakojaannos.roguelite.engine.view.audio.SoundEffect;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class LWJGLSoundEffect implements SoundEffect {
    private final int bufferPointer;
    private final AudioContext context;

    public LWJGLSoundEffect(
            final Path assetRoot,
            final String filename,
            final AudioContext context
    ) {
        this.context = context;

        // Load the sound effect
        final int channels;
        final int sampleRate;
        final ShortBuffer rawAudioBuffer;
        try (final var stack = stackPush()) {
            final var pChannels = stack.mallocInt(1);
            final var pSampleRate = stack.mallocInt(1);

            rawAudioBuffer = stb_vorbis_decode_filename(assetRoot.resolve("sounds")
                                                                 .resolve(filename)
                                                                 .toString(),
                                                        pChannels,
                                                        pSampleRate);

            channels = pChannels.get(0);
            sampleRate = pSampleRate.get(0);
        }

        final int format;
        if (channels == 1) {
            format = AL_FORMAT_MONO16;
        } else if (channels == 2) {
            format = AL_FORMAT_STEREO16;
        } else {
            throw new IllegalStateException("Unsupported audio channel count: " + channels);
        }

        this.bufferPointer = alGenBuffers();
        alBufferData(this.bufferPointer, format, rawAudioBuffer, sampleRate);
        free(rawAudioBuffer);
    }

    @Override
    public void play(final int priority, final float gain, final float pitch) {
        this.context.nextSource(priority)
                    .ifPresent(source -> {
                        alSourcef(source, AL_GAIN, gain);
                        alSourcef(source, AL_PITCH, pitch);

                        alSourcei(source, AL_BUFFER, this.bufferPointer);
                        alSourcePlay(source);
                    });
    }

    @Override
    public void close() {
        alDeleteBuffers(this.bufferPointer);
    }
}
