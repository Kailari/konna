package fi.jakojaannos.konna.engine.assets.storage;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import fi.jakojaannos.konna.engine.assets.AssetLoader;
import fi.jakojaannos.konna.engine.assets.AssetManager;
import fi.jakojaannos.konna.engine.assets.AssetStorage;
import fi.jakojaannos.konna.engine.assets.SkeletalMesh;
import fi.jakojaannos.konna.engine.assets.internal.Shader;
import fi.jakojaannos.konna.engine.assets.internal.ShaderLoader;

public class AssetManagerImpl implements AssetManager {

    private final Map<Class<?>, AssetStorage<?>> storages = new HashMap<>();

    private final Path assetRoot;

    public AssetManagerImpl(final Path assetRoot) {
        this.assetRoot = assetRoot;

        register(Shader.class, new ShaderLoader(), null);
        //register(SkeletalMesh.class, new ShaderLoader(), null);
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
