package fi.jakojaannos.konna.engine;

import java.nio.ByteBuffer;

import fi.jakojaannos.konna.engine.assets.Material;
import fi.jakojaannos.konna.engine.vulkan.TextureSampler;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorBinding;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.engine.vulkan.rendering.Swapchain;
import fi.jakojaannos.konna.engine.vulkan.descriptor.CombinedImageSamplerBinding;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorObject;
import fi.jakojaannos.konna.engine.vulkan.descriptor.UniformBufferBinding;

import static org.lwjgl.vulkan.VK10.*;

public class MaterialInstance extends DescriptorObject {
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

    public MaterialInstance(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout,
            final TextureSampler sampler,
            final Material material
    ) {
        super(deviceContext,
              swapchain,
              descriptorPool,
              layout,
              new CombinedImageSamplerBinding[]{
                      new CombinedImageSamplerBinding(TEXTURE_DESCRIPTOR_BINDING.slot(),
                                                      material.texture().getImageView(),
                                                      sampler)
              },
              new UniformBufferBinding[]{
                      new MaterialBinding(material)
              });
    }

    public static class MaterialBinding implements UniformBufferBinding {
        private static final int OFFSET_AMBIENT = 0;
        private static final int OFFSET_DIFFUSE = 4 * Float.BYTES;
        private static final int OFFSET_SPECULAR = 8 * Float.BYTES;
        private static final int OFFSET_REFLECTANCE = 12 * Float.BYTES;
        private static final int OFFSET_HAS_TEXTURE = 13 * Float.BYTES;

        private final Material material;

        private MaterialBinding(final Material material) {
            this.material = material;
        }

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
            this.material.ambient().get(offset + OFFSET_AMBIENT, buffer);
            this.material.diffuse().get(offset + OFFSET_DIFFUSE, buffer);
            this.material.specular().get(offset + OFFSET_SPECULAR, buffer);
            buffer.putFloat(offset + OFFSET_REFLECTANCE, this.material.reflectance());
            buffer.putInt(offset + OFFSET_HAS_TEXTURE, this.material.hasTexture() ? 1 : 0);
        }
    }
}
