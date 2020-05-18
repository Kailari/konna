package fi.jakojaannos.konna.engine.assets;

public interface AssetStorage<TAsset> extends AutoCloseable {
    TAsset get(String assetPath);

    @Override
    void close();
}
