package fi.jakojaannos.roguelite.engine.view.audio;

public interface SoundEffect extends AutoCloseable {
    void play(int priority, float gain, float pitch);
}
