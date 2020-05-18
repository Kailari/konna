package fi.jakojaannos.konna.engine.assets;

public interface AssetManager extends AutoCloseable {
    <TAsset> AssetStorage<TAsset> getStorage(Class<TAsset> assetClass);

    @Override
    void close();
}
