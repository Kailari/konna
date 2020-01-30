package fi.jakojaannos.roguelite.engine.content;

import java.util.function.BiConsumer;

public interface AssetRegistry<TAsset> extends AutoCloseable {
    TAsset getByAssetHandle(AssetHandle handle);

    default TAsset getByAssetName(final String name) {
        return getByAssetHandle(new AssetHandle(name));
    }

    void forEach(BiConsumer<AssetHandle, TAsset> action);
}
