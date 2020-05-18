package fi.jakojaannos.konna.engine.assets;

import java.nio.file.Path;
import java.util.Optional;

public interface AssetLoader<TAsset> {
    Optional<TAsset> load(Path path);
}
