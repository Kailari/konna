package fi.jakojaannos.roguelite.game.view.data;

import java.nio.ShortBuffer;
import java.nio.file.Path;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class SoundEffect implements AutoCloseable {
    private final int[] sourcePointers;
    private final int bufferPointer;

    private int pointer;

    public SoundEffect(final Path assetRoot, final String filename, final int sourceCount) {
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

        // Get the audio source(s)
        this.sourcePointers = new int[sourceCount];
        alGenSources(this.sourcePointers);
        for (final int source : this.sourcePointers) {
            alSourcei(source, AL_BUFFER, this.bufferPointer);
        }
    }

    public void play() {
        alSourcePlay(this.sourcePointers[this.pointer]);
        this.pointer++;
        if (this.pointer >= this.sourcePointers.length) {
            this.pointer = 0;
        }
    }

    @Override
    public void close() {
        alDeleteSources(this.sourcePointers);
        alDeleteBuffers(this.bufferPointer);
    }
}
