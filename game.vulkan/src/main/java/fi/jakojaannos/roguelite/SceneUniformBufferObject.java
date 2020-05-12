package fi.jakojaannos.roguelite;

import org.joml.Vector3f;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.nio.ByteBuffer;

import fi.jakojaannos.roguelite.util.RecreateCloseable;
import fi.jakojaannos.roguelite.vulkan.GPUBuffer;
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

public class SceneUniformBufferObject extends RecreateCloseable {
    private static final int MAX_LIGHTS = 10;
    private static final DescriptorBinding LIGHT_COUNT_DESCRIPTOR_BINDING = new DescriptorBinding(1,
                                                                                                  VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                                                                  1,
                                                                                                  VK_SHADER_STAGE_FRAGMENT_BIT);
    private static final DescriptorBinding LIGHTS_DESCRIPTOR_BINDING = new DescriptorBinding(0,
                                                                                             VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                                                             1,
                                                                                             VK_SHADER_STAGE_FRAGMENT_BIT);
    private final DeviceContext deviceContext;
    private final Swapchain swapchain;
    private final DescriptorPool descriptorPool;
    private final DescriptorSetLayout layout;

    private final long minUniformBufferOffsetAlignment;

    private final LightsBinding lights;
    private final LightCountBinding lightCount;
    private final UniformBinding[] bindings;

    private GPUBuffer[] buffers;
    private long[] bindingOffsets;
    private long[] descriptorSets;

    @Override
    protected boolean isRecreateRequired() {
        return this.isOlderThan(this.descriptorPool);
    }

    public DescriptorSetLayout getLayout() {
        return this.layout;
    }

    public SceneUniformBufferObject(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool
    ) {
        this.deviceContext = deviceContext;
        this.swapchain = swapchain;
        this.descriptorPool = descriptorPool;

        this.lights = new LightsBinding();
        this.lightCount = new LightCountBinding();
        this.bindings = new UniformBinding[]{this.lights, this.lightCount};
        this.layout = new DescriptorSetLayout(deviceContext,
                                              this.lights.getDescriptorBinding(),
                                              this.lightCount.getDescriptorBinding());

        this.minUniformBufferOffsetAlignment = deviceContext.getDeviceProperties()
                                                            .limits()
                                                            .minUniformBufferOffsetAlignment();

        this.lightCount.count = 2;
        this.lights.colors[0] = new Vector3f(1.0f, 0.05f, 0.0f);
        this.lights.positions[0] = new Vector3f(0.0f, 0.0f, 2.0f);
        this.lights.radius[0] = 6.5f;
        this.lights.colors[1] = new Vector3f(1.0f, 1.00f, 1.0f);
        this.lights.positions[1] = new Vector3f(5.5f, 0.0f, 2.0f);
        this.lights.radius[1] = 3.5f;
    }

    public long getDescriptorSet(final int imageIndex) {
        return this.descriptorSets[imageIndex];
    }

    @Override
    protected void recreate() {
        this.buffers = new GPUBuffer[this.swapchain.getImageCount()];
        this.bindingOffsets = new long[this.bindings.length];

        final var minAlign = this.minUniformBufferOffsetAlignment;
        for (int imageIndex = 0; imageIndex < this.buffers.length; imageIndex++) {
            var accumulatedSize = 0;

            for (int bindingIndex = 0; bindingIndex < this.bindings.length; bindingIndex++) {
                final var size = this.bindings[bindingIndex].getSizeInBytes();
                final var n = (int) Math.ceil(size / (double) minAlign);
                accumulatedSize += n * minAlign;

                // Update offset of the next binding, unless we are at the last binding already.
                // The first binding always has offset of zero.
                if (bindingIndex + 1 < this.bindings.length) {
                    this.bindingOffsets[bindingIndex + 1] = accumulatedSize;
                }
            }

            this.buffers[imageIndex] = new GPUBuffer(this.deviceContext,
                                                     accumulatedSize,
                                                     VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                                                     bitMask(VkMemoryPropertyFlags.HOST_VISIBLE_BIT,
                                                             VkMemoryPropertyFlags.HOST_COHERENT_BIT));

            try (final var stack = stackPush()) {
                final var data = stack.malloc((int) this.buffers[imageIndex].getSize());

                for (int bindingIndex = 0; bindingIndex < this.bindings.length; bindingIndex++) {
                    this.bindings[bindingIndex].write((int) this.bindingOffsets[bindingIndex], data);
                }
                this.buffers[imageIndex].push(data, 0, this.buffers[imageIndex].getSize());
            }
        }

        this.descriptorSets = this.descriptorPool.allocate(this.layout, this.swapchain.getImageCount());

        for (int imageIndex = 0; imageIndex < this.descriptorSets.length; ++imageIndex) {
            try (final var ignored = stackPush()) {
                final var descriptorWrites = VkWriteDescriptorSet.callocStack(2);

                final var buffer = this.buffers[imageIndex];
                for (int bindingIndex = 0; bindingIndex < this.bindings.length; ++bindingIndex) {
                    final var binding = this.bindings[bindingIndex];
                    final var descriptor = binding.getDescriptorBinding();

                    final var bufferInfos = VkDescriptorBufferInfo
                            .callocStack(1)
                            .buffer(buffer.getHandle())
                            .offset(this.bindingOffsets[bindingIndex])
                            .range(binding.getSizeInBytes());

                    descriptorWrites.get(bindingIndex)
                                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                                    .dstSet(this.descriptorSets[imageIndex])
                                    .dstBinding(descriptor.slot())
                                    .descriptorType(descriptor.descriptorType())
                                    .descriptorCount(descriptor.descriptorCount())
                                    .pBufferInfo(bufferInfos)
                                    .dstArrayElement(0);
                }

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

    @Override
    public void close() {
        super.close();
        this.layout.close();
    }

    private static class LightsBinding implements UniformBinding {
        private static final int OFFSET_POSITION = 0;
        private static final int OFFSET_RADIUS = 3 * Float.BYTES;
        private static final int OFFSET_COLOR = 4 * Float.BYTES;

        private static final int STRIDE = 8 * Float.BYTES;

        private final Vector3f[] positions = new Vector3f[MAX_LIGHTS];
        private final Vector3f[] colors = new Vector3f[MAX_LIGHTS];
        private final float[] radius = new float[MAX_LIGHTS];

        @Override
        public DescriptorBinding getDescriptorBinding() {
            return LIGHTS_DESCRIPTOR_BINDING;
        }

        @Override
        public long getSizeInBytes() {
            return MAX_LIGHTS * STRIDE;
        }

        @Override
        public void write(final int offset, final ByteBuffer buffer) {
            for (int i = 0; i < MAX_LIGHTS; ++i) {
                if (this.positions[i] == null) {
                    continue;
                }

                final var actualOffset = offset + STRIDE * i;

                this.positions[i].get(actualOffset + OFFSET_POSITION, buffer);
                this.colors[i].get(actualOffset + OFFSET_COLOR, buffer);
                buffer.putFloat(actualOffset + OFFSET_RADIUS, this.radius[i]);
            }
        }
    }

    private static class LightCountBinding implements UniformBinding {
        private static final int OFFSET_COUNT = 0;

        private int count;

        @Override
        public DescriptorBinding getDescriptorBinding() {
            return LIGHT_COUNT_DESCRIPTOR_BINDING;
        }

        @Override
        public long getSizeInBytes() {
            return Integer.BYTES;
        }

        @Override
        public void write(final int offset, final ByteBuffer buffer) {
            buffer.putInt(offset + OFFSET_COUNT, this.count);
        }
    }
}
