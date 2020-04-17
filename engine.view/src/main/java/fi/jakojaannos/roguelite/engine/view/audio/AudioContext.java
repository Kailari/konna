package fi.jakojaannos.roguelite.engine.view.audio;

import java.nio.file.Path;
import java.util.Optional;

public interface AudioContext extends AutoCloseable {
    Optional<Integer> nextSource(int priority);

    SoundEffect createEffect(Path assetRoot, String filename, AudioContext context);
}
