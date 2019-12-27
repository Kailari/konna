package fi.jakojaannos.roguelite.engine.view.content;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.jakojaannos.roguelite.engine.content.AbstractAssetRegistry;
import fi.jakojaannos.roguelite.engine.content.AssetHandle;
import fi.jakojaannos.roguelite.engine.view.LogCategories;
import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;
import fi.jakojaannos.roguelite.engine.view.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.sprite.serialization.SpriteDeserializer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/**
 * Handles loading sprites from assets-directory.
 */
@Slf4j
public class SpriteRegistry extends AbstractAssetRegistry<Sprite> {
    private final Path assetRoot;

    private final TextureRegistry textures;
    private final Sprite defaultSprite;

    public SpriteRegistry(
            final Path assetRoot,
            final TextureRegistry textures
    ) {
        this.assetRoot = assetRoot;
        this.textures = textures;

        this.defaultSprite = Sprite.ofSingleFrame(new TextureRegion(textures.getDefault(),
                                                                    0, 0,
                                                                    1, 1));
    }

    @Override
    protected Sprite getDefault() {
        return this.defaultSprite;
    }

    @Override
    protected Optional<Sprite> loadAsset(final AssetHandle handle) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Sprite.class, new SpriteDeserializer<>(textures))
                .create();
        val path = assetRoot.resolve(handle.getName() + ".json");
        try (val reader = new InputStreamReader(Files.newInputStream(path, StandardOpenOption.READ))) {
            LOG.trace(LogCategories.SPRITE_SERIALIZATION,
                      "Loading sprite {}", path.toString());
            return Optional.ofNullable(gson.fromJson(reader, Sprite.class));
        } catch (IOException e) {
            LOG.error("Reading sprite \"{}\" failed!", handle);
            LOG.error("Exception: ", e);
            return Optional.empty();
        }
    }

    @Override
    public void close() {
    }
}
