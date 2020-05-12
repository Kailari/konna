package fi.jakojaannos.roguelite.assets;

import org.joml.Matrix4f;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import fi.jakojaannos.roguelite.util.RecreateCloseable;
import fi.jakojaannos.roguelite.vulkan.GPUBuffer;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorBinding;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;
import fi.jakojaannos.roguelite.vulkan.types.VkMemoryPropertyFlags;

import static fi.jakojaannos.roguelite.util.BitMask.bitMask;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class AnimatedMesh extends RecreateCloseable {
    public static final DescriptorBinding BONE_DESCRIPTOR_BINDING = new DescriptorBinding(0,
                                                                                          VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                                                          1,
                                                                                          VK_SHADER_STAGE_VERTEX_BIT);
    private static final Logger LOG = LoggerFactory.getLogger(AnimatedMesh.class);
    private static final int MAX_BONES = 150;
    private static final long SIZE_IN_BYTES = MAX_BONES * 16 * Float.BYTES;

    private final DeviceContext deviceContext;
    private final Swapchain swapchain;
    private final DescriptorPool descriptorPool;
    private final DescriptorSetLayout layout;

    private final SkeletalMesh[] meshes;
    private final Map<String, Animation> animations;

    private GPUBuffer[] buffers;
    private long[] descriptorSets;

    @Override
    protected boolean isRecreateRequired() {
        return this.isOlderThan(this.descriptorPool);
    }

    public SkeletalMesh[] getMeshes() {
        return this.meshes;
    }

    public AnimatedMesh(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout,
            final SkeletalMesh[] meshes,
            final Map<String, Animation> animations
    ) {
        this.deviceContext = deviceContext;
        this.swapchain = swapchain;
        this.descriptorPool = descriptorPool;
        this.layout = layout;

        this.meshes = meshes;
        this.animations = animations;
        if (this.animations.size() == 0) {
            final var boneTransforms = new Matrix4f[MAX_BONES];
            for (int i = 0; i < boneTransforms.length; i++) {
                boneTransforms[i] = new Matrix4f().identity();
            }

            this.animations.put("idle", new Animation("idle",
                                                      List.of(new Animation.Frame(boneTransforms)),
                                                      0.0));
        }
    }

    public void setFrame(final int imageIndex, final String animationName, final int frameIndex) {
        try (final var stack = stackPush()) {
            final var data = stack.malloc((int) this.buffers[imageIndex].getSize());

            final var animation = this.animations.get(animationName);
            final var frame = animation.frames().get(frameIndex);
            for (int i = 0; i < frame.boneTransforms().length; i++) {
                final var matrix = frame.boneTransforms()[i];

                final var offset = i * 16 * Float.BYTES;
                matrix.get(offset, data);
            }
            this.buffers[imageIndex].push(data, 0, this.buffers[imageIndex].getSize());
        }
    }

    @Override
    protected void recreate() {
        for (final var mesh : this.meshes) {
            mesh.tryRecreate();
        }

        this.buffers = new GPUBuffer[this.swapchain.getImageCount()];
        for (int imageIndex = 0; imageIndex < this.buffers.length; imageIndex++) {
            this.buffers[imageIndex] = new GPUBuffer(this.deviceContext,
                                                     SIZE_IN_BYTES,
                                                     VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                                                     bitMask(VkMemoryPropertyFlags.HOST_VISIBLE_BIT,
                                                             VkMemoryPropertyFlags.HOST_COHERENT_BIT));

            final var defaultAnimationName = this.animations.isEmpty()
                    ? "idle"
                    : this.animations.keySet().iterator().next();
            setFrame(imageIndex, defaultAnimationName, 0);
        }

        this.descriptorSets = this.descriptorPool.allocate(this.layout, this.swapchain.getImageCount());

        for (int imageIndex = 0; imageIndex < this.descriptorSets.length; ++imageIndex) {
            try (final var ignored = stackPush()) {
                final var descriptorWrites = VkWriteDescriptorSet.callocStack(1);
                final var buffer = this.buffers[imageIndex];
                final var bufferInfos = VkDescriptorBufferInfo
                        .callocStack(1)
                        .buffer(buffer.getHandle())
                        .offset(0)
                        .range(SIZE_IN_BYTES);

                descriptorWrites.get(0)
                                .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                                .dstSet(this.descriptorSets[imageIndex])
                                .dstBinding(BONE_DESCRIPTOR_BINDING.slot())
                                .descriptorType(BONE_DESCRIPTOR_BINDING.descriptorType())
                                .descriptorCount(BONE_DESCRIPTOR_BINDING.descriptorCount())
                                .pBufferInfo(bufferInfos)
                                .dstArrayElement(0);

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
        for (final var mesh : this.meshes) {
            mesh.close();
        }
    }

    public long getBoneDescriptorSet(final int imageIndex) {
        return this.descriptorSets[imageIndex];
    }
}
