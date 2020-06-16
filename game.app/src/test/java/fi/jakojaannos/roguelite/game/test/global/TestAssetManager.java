package fi.jakojaannos.roguelite.game.test.global;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

import fi.jakojaannos.riista.assets.AssetLoader;
import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.assets.AssetStorage;
import fi.jakojaannos.riista.view.assets.SkeletalMesh;
import fi.jakojaannos.riista.view.assets.StaticMesh;
import fi.jakojaannos.riista.view.ui.UiElement;
import fi.jakojaannos.riista.vulkan.assets.storage.AssetStorageImpl;
import fi.jakojaannos.riista.vulkan.assets.ui.UiLoader;

import static org.mockito.Mockito.mock;

// FIXME: actual implementation
public class TestAssetManager implements AssetManager {
    private static final Logger LOG = LoggerFactory.getLogger(AssetManager.class);

    private final Map<Class<?>, AssetStorage<?>> storages = new HashMap<>();

    private final Path assetRoot;

    @Override
    public Path getRootPath() {
        return assetRoot;
    }

    public TestAssetManager() {
        this.assetRoot = Path.of("../assets");

        register(UiElement.class, new UiLoader());
        register(SkeletalMesh.class, path -> Optional.of(mock(SkeletalMesh.class)));
        register(StaticMesh.class, path -> Optional.of(mock(StaticMesh.class)));
    }

    private <TAsset> void register(
            final Class<TAsset> assetClass,
            final AssetLoader<TAsset> loader
    ) {
        register(assetClass, loader, (TAsset) null);
    }

    private <TAsset> void register(
            final Class<TAsset> assetClass,
            final AssetLoader<TAsset> loader,
            final String defaultAsset
    ) {
        register(assetClass, loader, loader.load(this.assetRoot.resolve(defaultAsset))
                                           .orElseGet(() -> {
                                               LOG.error("Loading default asset from path \""
                                                         + defaultAsset + "\" failed!");
                                               return null;
                                           }));
    }

    private <TAsset> void register(
            final Class<TAsset> assetClass,
            final AssetLoader<TAsset> loader,
            @Nullable final TAsset defaultAsset
    ) {
        this.storages.put(assetClass, new AssetStorageImpl<>(this.assetRoot, loader, defaultAsset));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TAsset> AssetStorage<TAsset> getStorage(final Class<TAsset> assetClass) {
        if (!this.storages.containsKey(assetClass)) {
            throw new IllegalStateException("Tried to get storage for unregistered asset type: \""
                                            + assetClass.getSimpleName() + "\"");
        }
        return (AssetStorage<TAsset>) this.storages.get(assetClass);
    }

    @Override
    public void close() {
        this.storages.forEach((clazz, storage) -> storage.close());
    }
}
