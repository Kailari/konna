package fi.jakojaannos.konna.engine.assets.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import fi.jakojaannos.konna.engine.assets.*;
import fi.jakojaannos.konna.engine.assets.internal.Shader;
import fi.jakojaannos.konna.engine.assets.internal.ShaderLoader;
import fi.jakojaannos.konna.engine.assets.mesh.skeletal.SkeletalMeshLoader;
import fi.jakojaannos.konna.engine.assets.mesh.staticmesh.StaticMeshLoader;
import fi.jakojaannos.konna.engine.assets.texture.TextureLoader;
import fi.jakojaannos.konna.engine.assets.ui.FontLoader;
import fi.jakojaannos.konna.engine.assets.ui.UiLoader;
import fi.jakojaannos.konna.engine.view.ui.UiElement;
import fi.jakojaannos.konna.engine.vulkan.RenderingBackend;
import fi.jakojaannos.konna.engine.vulkan.window.Window;

public class AssetManagerImpl implements AssetManager {
    private static final Logger LOG = LoggerFactory.getLogger(AssetManager.class);

    private final Map<Class<?>, AssetStorage<?>> storages = new HashMap<>();

    private final Path assetRoot;

    @Override
    public Path getRootPath() {
        return this.assetRoot;
    }

    public AssetManagerImpl(
            final RenderingBackend backend,
            final Window window,
            final Path assetRoot
    ) {
        this.assetRoot = assetRoot;

        register(Shader.class, new ShaderLoader());
        register(Texture.class, new TextureLoader(backend), "textures/vulkan/texture.jpg");
        register(StaticMesh.class, new StaticMeshLoader(backend, this));
        register(SkeletalMesh.class, new SkeletalMeshLoader(backend, this));
        register(UiElement.class, new UiLoader());
        register(Font.class, new FontLoader(backend, window), "fonts/VCR_OSD_MONO.ttf");
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
