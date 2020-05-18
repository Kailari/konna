package fi.jakojaannos.konna.engine.assets.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import fi.jakojaannos.konna.engine.assets.AssetLoader;
import fi.jakojaannos.konna.engine.assets.AssetStorage;

public class AssetStorageImpl<TAsset> implements AssetStorage<TAsset> {
    private static final Logger LOG = LoggerFactory.getLogger(AssetStorage.class);

    private final Map<String, TAsset> loadedAssets = new HashMap<>();

    private final Path assetRoot;
    private final AssetLoader<TAsset> loader;
    @Nullable private final TAsset defaultAsset;

    public AssetStorageImpl(
            final Path assetRoot,
            final AssetLoader<TAsset> loader,
            @Nullable final TAsset defaultAsset
    ) {
        this.assetRoot = assetRoot;
        this.loader = loader;
        this.defaultAsset = defaultAsset;
    }

    @Override
    public TAsset get(final String assetPath) {
        return this.loadedAssets.computeIfAbsent(assetPath, this::loadAsset);
    }

    private TAsset loadAsset(final String path) {
        if (this.defaultAsset != null) {
            return this.loader.load(this.assetRoot.resolve(path))
                              .orElseGet(() -> {
                                  LOG.error("Asset with path \"{}\" not found! Falling back to default.",
                                            path);
                                  return this.defaultAsset;
                              });
        }

        return this.loader.load(this.assetRoot.resolve(path))
                          .orElseThrow(() -> new IllegalStateException(
                                  "Asset \"" + path + "\" not found or could not be loaded. "
                                  + "Not recoverable as default asset of this type has not been specified."));
    }

    @Override
    public void close() {
        this.loadedAssets.values().forEach(AssetStorageImpl::tryCloseAsset);
    }

    private static <TAsset> void tryCloseAsset(final TAsset asset) {
        if (asset instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (final Exception ignored) {
            }
        }
    }
}
