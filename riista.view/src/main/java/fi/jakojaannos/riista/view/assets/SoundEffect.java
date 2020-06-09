package fi.jakojaannos.riista.view.assets;

public interface SoundEffect extends AutoCloseable {
    void play(int priority, float gain, float pitch);
}
