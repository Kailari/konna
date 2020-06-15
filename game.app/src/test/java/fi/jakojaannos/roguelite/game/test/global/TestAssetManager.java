package fi.jakojaannos.roguelite.game.test.global;

import java.nio.file.Path;
import javax.annotation.Nullable;

import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.assets.AssetStorage;

// FIXME: actual implementation
public class TestAssetManager implements AssetManager {
    private final Path assetRoot;

    @Override
    public Path getRootPath() {
        return assetRoot;
    }

    public TestAssetManager() {
        this.assetRoot = Path.of("../assets");
    }

    @Override
    public <TAsset> AssetStorage<TAsset> getStorage(final Class<TAsset> assetClass) {
        return new AssetStorage<>() {
            @Override
            public TAsset getOrDefault(final String assetPath) {
                return null;
            }

            @Nullable
            @Override
            public TAsset getOrNull(final String path) {
                return null;
            }

            @Override
            public void close() {

            }
        };
    }

    @Override
    public void close() {
    }
}
