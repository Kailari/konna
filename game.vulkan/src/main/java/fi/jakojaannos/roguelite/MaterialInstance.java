package fi.jakojaannos.roguelite;

import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.nio.ByteBuffer;

import fi.jakojaannos.roguelite.assets.Material;
import fi.jakojaannos.roguelite.util.RecreateCloseable;
import fi.jakojaannos.roguelite.vulkan.GPUBuffer;
import fi.jakojaannos.roguelite.vulkan.TextureSampler;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorBinding;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;
import fi.jakojaannos.roguelite.vulkan.types.VkMemoryPropertyFlags;
import fi.jakojaannos.roguelite.vulkan.uniform.UniformBinding;

import static fi.jakojaannos.roguelite.util.BitMask.bitMask;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class MaterialInstance extends RecreateCloseable {
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

    private final DeviceContext deviceContext;
    private final Swapchain swapchain;

    private final DescriptorSetLayout layout;
    private final TextureSampler sampler;
    private final DescriptorPool descriptorPool;

    private final MaterialBinding materialBinding;

    private long[] descriptorSets;
    private GPUBuffer[] buffers;

    @Override
    public boolean isRecreateRequired() {
        return this.isOlderThan(this.descriptorPool);
    }

    public MaterialInstance(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout,
            final TextureSampler sampler,
            final Material material
    ) {
        this.deviceContext = deviceContext;
        this.swapchain = swapchain;
        this.descriptorPool = descriptorPool;

        this.sampler = sampler;

        this.materialBinding = new MaterialBinding(material);
        this.layout = layout;
    }

    public long getDescriptorSet(final int imageIndex) {
        return this.descriptorSets[imageIndex];
    }

    @Override
    protected void recreate() {
        this.buffers = new GPUBuffer[this.swapchain.getImageCount()];
        for (int imageIndex = 0; imageIndex < this.buffers.length; imageIndex++) {
            this.buffers[imageIndex] = new GPUBuffer(this.deviceContext,
                                                     this.materialBinding.getSizeInBytes(),
                                                     VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                                                     bitMask(VkMemoryPropertyFlags.HOST_VISIBLE_BIT,
                                                             VkMemoryPropertyFlags.HOST_COHERENT_BIT));

            try (final var stack = stackPush()) {
                final var buffer = this.buffers[imageIndex];

                final var data = stack.malloc((int) buffer.getSize());
                this.materialBinding.write(0, data);

                buffer.push(data, 0, buffer.getSize());
            }
        }

        this.descriptorSets = this.descriptorPool.allocate(this.layout, this.swapchain.getImageCount());

        for (int imageIndex = 0; imageIndex < this.descriptorSets.length; ++imageIndex) {
            final var buffer = this.buffers[imageIndex];
            final var descriptorSet = this.descriptorSets[imageIndex];

            try (final var ignored = stackPush()) {


                final var texture = this.materialBinding.material.texture();
                final var descriptorWrites = VkWriteDescriptorSet.callocStack(2);

                final var imageView = texture.getImageView();
                descriptorWrites.get(0)
                                .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                                .dstSet(descriptorSet)
                                .dstBinding(0)
                                .dstArrayElement(0)
                                .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                                .descriptorCount(1)
                                .pImageInfo(VkDescriptorImageInfo
                                                    .callocStack(1)
                                                    .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                                                    .imageView(imageView.getHandle())
                                                    .sampler(this.sampler.getHandle()));

                final var binding = this.materialBinding;
                final var descriptorBinding = binding.getDescriptorBinding();
                descriptorWrites.get(1)
                                .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                                .dstSet(descriptorSet)
                                .dstBinding(1)
                                .dstArrayElement(0)
                                .descriptorType(descriptorBinding.descriptorType())
                                .descriptorCount(1)
                                .pBufferInfo(VkDescriptorBufferInfo
                                                     .callocStack(1)
                                                     .buffer(buffer.getHandle())
                                                     .offset(0)
                                                     .range(binding.getSizeInBytes()));

                vkUpdateDescriptorSets(this.deviceContext.getDevice(), descriptorWrites, null);
            }
        }
    }

    @Override
    protected void cleanup() {
        for (final var buffer : this.buffers) {
            buffer.close();
        }
    }

    public static class MaterialBinding implements UniformBinding {
        private static final int OFFSET_AMBIENT = 0;
        private static final int OFFSET_DIFFUSE = 4 * Float.BYTES;
        private static final int OFFSET_SPECULAR = 8 * Float.BYTES;
        private static final int OFFSET_REFLECTANCE = 12 * Float.BYTES;
        private static final int OFFSET_HAS_TEXTURE = 13 * Float.BYTES;

        private final Material material;

        @Override
        public long getSizeInBytes() {
            // vec4 ambient, diffuse, specular
            // float reflectance
            // boolean(int) hasTexture
            // -> (3 * vec4) + (1 * float) + (1 * int)
            return 3 * 4 * Float.BYTES + Float.BYTES + Integer.BYTES;
        }

        @Override
        public DescriptorBinding getDescriptorBinding() {
            return MATERIAL_DESCRIPTOR_BINDING;
        }

        private MaterialBinding(final Material material) {
            this.material = material;
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
