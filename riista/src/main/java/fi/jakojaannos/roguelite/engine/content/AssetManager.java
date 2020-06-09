package fi.jakojaannos.roguelite.engine.content;

public interface AssetManager extends AutoCloseable {
    <TAsset> AssetRegistry<TAsset> getAssetRegistry(Class<TAsset> assetClass);
}
