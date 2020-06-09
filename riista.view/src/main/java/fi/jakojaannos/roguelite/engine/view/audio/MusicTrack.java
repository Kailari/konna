package fi.jakojaannos.roguelite.engine.view.audio;

public interface MusicTrack extends AutoCloseable {
    int getChannelCount();

    int getFormat();

    int getSampleRate();

    void rewind();

    void progressBy(int samples);
}
