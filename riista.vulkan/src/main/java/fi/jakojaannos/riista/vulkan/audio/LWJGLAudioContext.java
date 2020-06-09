package fi.jakojaannos.riista.vulkan.audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

import fi.jakojaannos.riista.view.audio.AudioContext;
import fi.jakojaannos.riista.view.audio.MusicPlayer;
import fi.jakojaannos.riista.view.assets.MusicTrack;
import fi.jakojaannos.riista.view.assets.SoundEffect;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class LWJGLAudioContext implements AudioContext {
    private final long context;
    private final long device;

    private final int[] sources;
    private final int[] priorities;
    private final int musicSource;

    public long getALContext() {
        return this.context;
    }

    public LWJGLAudioContext(final int nSources) {
        // Create device/context
        final var defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        this.device = alcOpenDevice(defaultDeviceName);

        final int[] attributes = {0};
        this.context = alcCreateContext(this.device, attributes);
        alcMakeContextCurrent(this.context);

        final var alcCapabilities = ALC.createCapabilities(this.device);
        AL.createCapabilities(alcCapabilities);

        // Get the audio source(s)
        this.musicSource = alGenSources();
        this.sources = new int[nSources];
        alGenSources(this.sources);

        this.priorities = new int[nSources];
        Arrays.fill(this.priorities, -1);
    }

    @Override
    public SoundEffect createEffect(
            final Path assetRoot,
            final String filename
    ) {
        return new LWJGLSoundEffect(assetRoot, filename, this);
    }

    @Override
    public Optional<Integer> nextSource(final int priority) {
        // Try to find free sources. If no free sources are available, pick the one with lowest priority.
        int lowestPriority = priority;
        int lowestIndex = -1;
        for (int i = 0; i < this.sources.length; i++) {
            if (this.priorities[i] <= lowestPriority) {
                lowestPriority = this.priorities[i];
                lowestIndex = i;
            }

            final var source = this.sources[i];
            final float state = getSourceState(source);
            if (state == AL_STOPPED) {
                this.priorities[i] = priority;
                return Optional.of(source);
            }
        }

        // No free sources, pick the lowest priority
        if (lowestIndex != -1) {
            this.priorities[lowestIndex] = priority;

            final var source = this.sources[lowestIndex];
            alSourceStop(source);
            return Optional.of(source);
        }

        return Optional.empty();
    }

    private int getSourceState(final int source) {
        final int state;
        try (final var stack = stackPush()) {
            final var pState = stack.mallocInt(1);
            alGetSourcei(source, AL_SOURCE_STATE, pState);
            state = pState.get(0);
        }
        return state;
    }

    @Override
    public MusicPlayer createMusicPlayer() {
        return new LWJGLMusicPlayer(this);
    }

    @Override
    public MusicTrack createTrack(final Path path) {
        return new LWJGLMusicTrack(path);
    }

    @Override
    public void close() {
        alDeleteSources(this.sources);
        alcDestroyContext(this.context);
        alcCloseDevice(this.device);
    }

    public int getMusicSource() {
        return this.musicSource;
    }
}
