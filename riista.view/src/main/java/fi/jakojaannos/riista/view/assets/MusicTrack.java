package fi.jakojaannos.riista.view.assets;

public interface MusicTrack extends AutoCloseable {
    int getChannelCount();

    int getFormat();

    int getSampleRate();

    void rewind();

    void progressBy(int samples);

    @Override
    void close();
}
