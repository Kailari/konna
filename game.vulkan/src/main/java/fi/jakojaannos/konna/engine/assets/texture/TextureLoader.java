package fi.jakojaannos.konna.engine.assets.texture;

import java.nio.file.Path;
import java.util.Optional;

import fi.jakojaannos.konna.engine.assets.AssetLoader;
import fi.jakojaannos.konna.engine.assets.Texture;
import fi.jakojaannos.konna.engine.vulkan.RenderingBackend;
import fi.jakojaannos.konna.engine.vulkan.types.VkImageAspectFlags;
import fi.jakojaannos.konna.engine.vulkan.types.VkImageUsageFlags;

import static fi.jakojaannos.konna.engine.util.BitMask.bitMask;

public class TextureLoader implements AssetLoader<Texture> {
    private final RenderingBackend backend;

    public TextureLoader(final RenderingBackend backend) {
        this.backend = backend;
    }

    @Override
    public Optional<Texture> load(final Path path) {
        // FIXME: Move texture loading here for better error handling
        return Optional.of(new TextureImpl(this.backend.deviceContext(), path));
    }
}
