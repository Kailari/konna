package fi.jakojaannos.riista.view.audio;

import java.nio.file.Path;
import java.util.Optional;

import fi.jakojaannos.riista.view.assets.MusicTrack;
import fi.jakojaannos.riista.view.assets.SoundEffect;

public interface AudioContext extends AutoCloseable {
    Optional<Integer> nextSource(int priority);

    SoundEffect createEffect(Path assetRoot, String filename);

    MusicPlayer createMusicPlayer();

    MusicTrack createTrack(Path path);

    @Override
    void close();
}
