package fi.jakojaannos.riista.vulkan.assets.material;

import org.joml.Vector4f;

import java.nio.ByteBuffer;
import javax.annotation.Nullable;

import fi.jakojaannos.riista.view.assets.Material;
import fi.jakojaannos.riista.view.assets.Texture;
import fi.jakojaannos.riista.vulkan.assets.texture.TextureImpl;
import fi.jakojaannos.riista.vulkan.internal.RenderingBackend;
import fi.jakojaannos.riista.vulkan.internal.TextureSampler;
import fi.jakojaannos.riista.vulkan.internal.descriptor.*;
import fi.jakojaannos.riista.vulkan.internal.device.DeviceContext;
import fi.jakojaannos.riista.vulkan.rendering.ImageView;

import static org.lwjgl.vulkan.VK10.*;

public class MaterialDescriptor extends DescriptorObject {
    public static final DescriptorBinding TEXTURE_DESCRIPTOR_BINDING = new DescriptorBinding(
            0,
            VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
            1,
            VK_SHADER_STAGE_FRAGMENT_BIT
    );
    public static final DescriptorBinding MATERIAL_DESCRIPTOR_BINDING = new DescriptorBinding(
            1,
            VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
            1,
            VK_SHADER_STAGE_FRAGMENT_BIT
    );

    private final TextureBinding textureBinding;
    private final MaterialBinding materialBinding;

    public MaterialDescriptor(
            final RenderingBackend backend,
            final Texture defaultTexture,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout,
            final TextureSampler sampler
    ) {
        this(backend.deviceContext(),
             descriptorPool,
             layout,
             new TextureBinding(TEXTURE_DESCRIPTOR_BINDING.slot(), sampler, defaultTexture),
             new MaterialBinding());
    }

    private MaterialDescriptor(
            final DeviceContext deviceContext,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout,
            final TextureBinding textureBinding,
            final MaterialBinding materialBinding
    ) {
        super(deviceContext,
              () -> 1,
              descriptorPool,
              layout,
              new CombinedImageSamplerBinding[]{
                      textureBinding
              },
              new UniformBufferBinding[]{
                      materialBinding
              });

        this.textureBinding = textureBinding;
        this.materialBinding = materialBinding;
    }

    public void update(final Material material) {
        this.materialBinding.ambient.set(material.ambient());
        this.materialBinding.diffuse.set(material.diffuse());
        this.materialBinding.specular.set(material.specular());
        this.materialBinding.reflectance = material.reflectance();

        this.materialBinding.hasTexture = material.texture() != null;
        this.textureBinding.texture = (TextureImpl) material.texture();

        flushAllUniformBufferBindings(0);
        flushAllCombinedImageSamplerBindings(0);
    }

    public static class MaterialBinding implements UniformBufferBinding {
        private static final int OFFSET_AMBIENT = 0;
        private static final int OFFSET_DIFFUSE = 4 * Float.BYTES;
        private static final int OFFSET_SPECULAR = 8 * Float.BYTES;
        private static final int OFFSET_REFLECTANCE = 12 * Float.BYTES;
        private static final int OFFSET_HAS_TEXTURE = 13 * Float.BYTES;

        private final Vector4f ambient = new Vector4f();
        private final Vector4f diffuse = new Vector4f();
        private final Vector4f specular = new Vector4f();
        private boolean hasTexture;
        private float reflectance;

        @Override
        public long sizeInBytes() {
            // vec4 ambient, diffuse, specular
            // float reflectance
            // boolean(int) hasTexture
            // -> (3 * vec4) + (1 * float) + (1 * int)
            return 3 * 4 * Float.BYTES + Float.BYTES + Integer.BYTES;
        }

        @Override
        public int binding() {
            return MATERIAL_DESCRIPTOR_BINDING.slot();
        }

        @Override
        public void write(final int offset, final ByteBuffer buffer) {
            this.ambient.get(offset + OFFSET_AMBIENT, buffer);
            this.diffuse.get(offset + OFFSET_DIFFUSE, buffer);
            this.specular.get(offset + OFFSET_SPECULAR, buffer);
            buffer.putFloat(offset + OFFSET_REFLECTANCE, this.reflectance);
            buffer.putInt(offset + OFFSET_HAS_TEXTURE, this.hasTexture ? 1 : 0);
        }
    }

    private static class TextureBinding implements CombinedImageSamplerBinding {
        private final int slot;
        private final TextureSampler sampler;
        private final TextureImpl defaultTexture;

        @Nullable private TextureImpl texture;

        public TextureBinding(final int slot, final TextureSampler sampler, final Texture defaultTexture) {
            this.slot = slot;
            this.sampler = sampler;

            // FIXME: Remove cast
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
