package fi.jakojaannos.riista.vulkan.renderer.ui;

import fi.jakojaannos.riista.view.assets.FontTexture;
import fi.jakojaannos.riista.view.assets.Texture;
import fi.jakojaannos.konna.engine.vulkan.RenderingBackend;
import fi.jakojaannos.konna.engine.vulkan.TextureSampler;
import fi.jakojaannos.konna.engine.vulkan.descriptor.*;
import fi.jakojaannos.riista.vulkan.assets.texture.TextureImpl;
import fi.jakojaannos.riista.vulkan.assets.ui.FontTextureImpl;
import fi.jakojaannos.riista.vulkan.rendering.ImageView;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

public class FontDescriptor extends DescriptorObject {
    public static final DescriptorBinding FONT_TEXTURE_DESCRIPTOR_BINDING = new DescriptorBinding(
            0,
            VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
            1,
            VK_SHADER_STAGE_FRAGMENT_BIT
    );

    private final TextureBinding textureBinding;

    public FontDescriptor(
            final RenderingBackend backend,
            final Texture defaultTexture,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout,
            final TextureSampler sampler
    ) {
        this(backend,
             descriptorPool,
             layout,
             new TextureBinding(FONT_TEXTURE_DESCRIPTOR_BINDING.slot(), sampler, defaultTexture));
    }

    private FontDescriptor(
            final RenderingBackend backend,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout,
            final TextureBinding textureBinding
    ) {
        super(backend.deviceContext(),
              backend.swapchain(),
              descriptorPool,
              layout,
              new CombinedImageSamplerBinding[]{
                      textureBinding
              },
              new UniformBufferBinding[0]);
        this.textureBinding = textureBinding;
    }

    public void update(final FontTexture fontTexture, final int imageIndex) {
        // FIXME: Get rid of the cast
        this.textureBinding.texture = (FontTextureImpl) fontTexture;

        flushAllCombinedImageSamplerBindings(imageIndex);
    }

    private static class TextureBinding implements CombinedImageSamplerBinding {
        private final int slot;
        private final TextureSampler sampler;
        private final TextureImpl defaultTexture;

        // FIXME: Weaken to `Texture`
        private FontTextureImpl texture;

        public TextureBinding(
                final int slot,
                final TextureSampler sampler,
                final Texture defaultTexture
        ) {
            this.slot = slot;
            this.sampler = sampler;

            // FIXME: Get rid of the cast
            this.defaultTexture = (TextureImpl) defaultTexture;
        }

        @Override
        public int binding() {
            return this.slot;
        }

        @Override
        public ImageView imageView() {
            return this.texture == null
                    ? this.defaultTexture.getImageView()
                    : this.texture.getImageView();
        }

        @Override
        public TextureSampler sampler() {
            return this.sampler;
        }
    }
}
