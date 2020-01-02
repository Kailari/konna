package fi.jakojaannos.roguelite.game.test.content;

import fi.jakojaannos.roguelite.engine.content.AssetManager;
import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
import fi.jakojaannos.roguelite.engine.view.content.FontRegistry;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.view.content.TextureRegistry;
import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.rendering.text.Font;
import fi.jakojaannos.roguelite.game.test.view.rendering.TestTexture;
import fi.jakojaannos.roguelite.game.test.view.rendering.text.TestFont;
import lombok.val;

import java.nio.file.Path;
import java.util.Map;

public class TestAssetManager implements AssetManager {
    private final Map<Class<?>, AssetRegistry<?>> registries;

    public TestAssetManager(final Path assetRoot) {
        val textureRegistry = new TextureRegistry(assetRoot, TestTexture::new);
        this.registries = Map.ofEntries(
                Map.entry(Texture.class, textureRegistry),
                Map.entry(Sprite.class, new SpriteRegistry(assetRoot, textureRegistry)),
                Map.entry(Font.class, new FontRegistry(assetRoot, TestFont::new))
        );
    }

    @Override
    public <TAsset> AssetRegistry<TAsset> getAssetRegistry(final Class<TAsset> assetClass) {
        if (!this.registries.containsKey(assetClass)) {
            throw new IllegalStateException("Unknown asset type: " + assetClass.getSimpleName());
        }

        //noinspection unchecked
        return (AssetRegistry<TAsset>) this.registries.get(assetClass);
    }

    @Override
    public void close() {
        this.registries.values().forEach(assetRegistry -> {
            try { assetRegistry.close(); } catch (Exception ignored) {}
        });
    }
}
