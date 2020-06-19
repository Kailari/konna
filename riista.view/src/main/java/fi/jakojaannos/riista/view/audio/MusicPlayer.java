package fi.jakojaannos.riista.view.audio;

import fi.jakojaannos.riista.view.assets.MusicTrack;

public interface MusicPlayer extends AutoCloseable {
    void nextTrack(MusicTrack track);

    void playNow(MusicTrack track);

    void update();

    @Override
    void close();
}
