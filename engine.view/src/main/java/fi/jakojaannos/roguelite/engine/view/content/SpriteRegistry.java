package fi.jakojaannos.roguelite.engine.view.content;

import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import fi.jakojaannos.roguelite.engine.content.AbstractAssetRegistry;
import fi.jakojaannos.roguelite.engine.content.AssetHandle;
import fi.jakojaannos.roguelite.engine.view.LogCategories;
import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.serialization.SpriteDeserializer;

/**
 * Handles loading sprites from assets-directory.
 */
public class SpriteRegistry extends AbstractAssetRegistry<Sprite> {
    private static final Logger LOG = LoggerFactory.getLogger(SpriteRegistry.class);

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
        final var gson = new GsonBuilder()
                .registerTypeAdapter(Sprite.class, new SpriteDeserializer<>(this.textures))
                .create();
        final var path = this.assetRoot.resolve(handle.name() + ".json");
        try (final var reader = new InputStreamReader(Files.newInputStream(path, StandardOpenOption.READ))) {
            LOG.trace(LogCategories.SPRITE_SERIALIZATION,
                      "Loading sprite {}", path.toString());
            return Optional.ofNullable(gson.fromJson(reader, Sprite.class));
        } catch (final IOException e) {
            LOG.error("Reading sprite \"{}\" failed!", handle);
            LOG.error("Exception: ", e);
            return Optional.empty();
        }
    }

    @Override
    public void close() {
    }
}
