package fi.jakojaannos.roguelite.engine.view.content;

import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.imageio.ImageIO;

import fi.jakojaannos.roguelite.engine.content.AbstractAssetRegistry;
import fi.jakojaannos.roguelite.engine.content.AssetHandle;
import fi.jakojaannos.roguelite.engine.view.rendering.Texture;

@Slf4j
public class TextureRegistry extends AbstractAssetRegistry<Texture> {
    private final Path assetRoot;
    private final TextureLoader<Texture> textureLoader;
    private final Texture defaultTexture;

    public TextureRegistry(final Path assetRoot, final TextureLoader<Texture> textureLoader) {
        this.assetRoot = assetRoot;
        this.textureLoader = textureLoader;
        this.defaultTexture = getByAssetName("textures/sheep.png");
    }

    @Override
    protected Texture getDefault() {
        return this.defaultTexture;
    }

    @Override
    protected Optional<Texture> loadAsset(final AssetHandle handle) {
        final var path = this.assetRoot.resolve(handle.getName());
        try (final var inputStream = Files.newInputStream(path)) {
            final var image = ImageIO.read(inputStream);
            final var width = image.getWidth();
            final var height = image.getHeight();
            return Optional.of(this.textureLoader.load(width, height, image));
        } catch (final IOException e) {
            LOG.warn("Image in path \"{}\" could not be opened!", path.toString());
            return Optional.empty();
        }
    }

    @Override
    public void close() throws Exception {
        forEach((handle, texture) -> {
            try {
                texture.close();
            } catch (final Exception ignored) {
            }
        });
        super.close();
    }

    public interface TextureLoader<TTexture extends Texture> {
        TTexture load(int width, int height, BufferedImage image);
    }
}
