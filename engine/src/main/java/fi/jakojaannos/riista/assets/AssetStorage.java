package fi.jakojaannos.riista.assets;

import javax.annotation.Nullable;

public interface AssetStorage<TAsset> extends AutoCloseable {
    TAsset getOrDefault(String assetPath);

    @Override
    void close();

    @Nullable
    TAsset getOrNull(String path);
}
