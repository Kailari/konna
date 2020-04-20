package fi.jakojaannos.roguelite.engine.view.audio;

public interface MusicPlayer extends AutoCloseable {
    void nextTrack(MusicTrack track);

    void playNow(MusicTrack track);

    void update();
}
