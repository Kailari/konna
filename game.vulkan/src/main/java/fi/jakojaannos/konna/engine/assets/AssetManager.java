package fi.jakojaannos.konna.engine.assets;

import java.nio.file.Path;

public interface AssetManager extends AutoCloseable {
    <TAsset> AssetStorage<TAsset> getStorage(Class<TAsset> assetClass);

    @Override
    void close();

    Path getRootPath();
}
