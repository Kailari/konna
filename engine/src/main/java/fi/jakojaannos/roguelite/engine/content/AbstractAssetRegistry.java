package fi.jakojaannos.roguelite.engine.content;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public abstract class AbstractAssetRegistry<TAsset> implements AssetRegistry<TAsset> {
    private final Map<AssetHandle, TAsset> assets = new HashMap<>();

    protected abstract TAsset getDefault();

    protected abstract Optional<TAsset> loadAsset(AssetHandle handle);

    protected TAsset loadAssetOrDefault(final AssetHandle handle) {
        return loadAsset(handle).orElseGet(this::getDefault);
    }

    @Override
    public TAsset getByAssetHandle(final AssetHandle handle) {
        return this.assets.computeIfAbsent(handle, this::loadAssetOrDefault);
    }

    @Override
    public void forEach(final BiConsumer<AssetHandle, TAsset> action) {
        this.assets.forEach(action);
    }

    @Override
    public void close() throws Exception {
        this.assets.clear();
    }
}
