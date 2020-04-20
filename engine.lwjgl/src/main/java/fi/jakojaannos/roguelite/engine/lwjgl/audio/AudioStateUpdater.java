package fi.jakojaannos.roguelite.engine.lwjgl.audio;

interface AudioStateUpdater {
    void update(LWJGLMusicTrack track);

    boolean play(final LWJGLMusicTrack track);
}
