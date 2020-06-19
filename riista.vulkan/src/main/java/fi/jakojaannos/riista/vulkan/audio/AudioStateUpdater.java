package fi.jakojaannos.riista.vulkan.audio;

interface AudioStateUpdater {
    void update(LWJGLMusicTrack track);

    boolean play(final LWJGLMusicTrack track);
}
