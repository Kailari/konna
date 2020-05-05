package fi.jakojaannos.roguelite.vulkan.uniform;

import org.lwjgl.vulkan.*;

import fi.jakojaannos.roguelite.util.RecreateCloseable;
import fi.jakojaannos.roguelite.vulkan.GPUBuffer;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;

import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Collection of shader uniforms, stored in a GPU buffer.
 * <p>
 * Each UBO contains one or more <i>bindings</i>, which each describe a separate uniform block/variable on the shader.
 * How these bindings are used is described by <i>the descriptor set layout</i>. The layout can then be used within a
 * {@link fi.jakojaannos.roguelite.vulkan.rendering.GraphicsPipeline Graphics Pipeline} to describe what kind of
 * uniforms the pipeline expects to receive.
 * <p>
 * To update the UBO, use the {@link UniformBinding#update(int, int, Object) update} method on the bindings. Behind the
 * scenes, contents of the UBO are updated by pushing the data to the underlying {@link GPUBuffer buffer}. This buffer
 * is then referred to by the descriptor sets.
 * <p>
 * To actually use the uniforms, a descriptor set must be allocated using a {@link DescriptorPool Descriptor Pool}. The
 * set is then configured to point to the appropriate buffer and then {@link org.lwjgl.vulkan.VK11#vkCmdBindDescriptorSets(VkCommandBuffer,
 * int, long, int, long[], int[]) bound}, after which the uniform data is visible to shaders.
 */
public class UniformBufferObject extends RecreateCloseable {
    private final DeviceContext deviceContext;
    private final Swapchain swapchain;
    private final DescriptorPool descriptorPool;

    private final UniformBinding<?>[] bindings;

    private long descriptorSetLayout;
    private long[] descriptorSets;

    @Override
    public boolean isRecreateRequired() {
        return true;
    }

    public long getLayoutHandle() {
        return this.descriptorSetLayout;
    }

    public UniformBufferObject(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final UniformBinding<?>... bindings
    ) {
        this.deviceContext = deviceContext;
        this.swapchain = swapchain;
        this.descriptorPool = descriptorPool;
        this.bindings = bindings;

        tryRecreate();
    }

    public long getDescriptorSet(final int imageIndex) {
        return this.descriptorSets[imageIndex];
    }

    @Override
    protected void recreate() {
        for (final var binding : this.bindings) {
            binding.tryRecreate();
        }

        this.descriptorSetLayout = createDescriptorSetLayout();
        this.descriptorSets = allocateDescriptorSets();

        for (int imageIndex = 0; imageIndex < this.descriptorSets.length; imageIndex++) {
            // FIXME: This is basically "full write" for all bindings?
            for (final var binding : this.bindings) {
                final var buffer = binding.getBuffer(imageIndex);
                final var bufferInfo = VkDescriptorBufferInfo
                        .callocStack(1)
                        .buffer(buffer.getHandle())
                        .offset(0)
                        .range(buffer.getSize());

                final var descriptorWrites = VkWriteDescriptorSet
                        .callocStack(1)
                        .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                        .dstSet(this.descriptorSets[imageIndex])
                        .dstBinding(binding.getBinding())
                        .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                        .descriptorCount(1)
                        .pBufferInfo(bufferInfo)
                        .dstArrayElement(0);

                // NOTE: Why don't we need to call this every frame? Well, the bindings allocate the buffers
                //       as HOST_COHERENT, so we point the GPU at the memory once and it knows where to look
                //       from there on.
                // FIXME: This should happen in binding update(), too (in cases where backing buffer is not HOST_COHERENT)
                vkUpdateDescriptorSets(this.deviceContext.getDevice(), descriptorWrites, null);
            }
        }
    }

    private long createDescriptorSetLayout() {
        try (final var stack = stackPush()) {
            final var layoutBindings = VkDescriptorSetLayoutBinding.callocStack(this.bindings.length);
            for (int i = 0; i < this.bindings.length; i++) {
                final var binding = this.bindings[i];
                layoutBindings.get(i)
                              .binding(binding.getBinding())
                              .descriptorType(binding.getDescriptorType())
                              .descriptorCount(binding.getDescriptorCount())
                              .stageFlags(binding.getStageFlags());
            }

            final var createInfo = VkDescriptorSetLayoutCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
                    .pBindings(layoutBindings);

            final var pLayout = stack.mallocLong(1);
            ensureSuccess(vkCreateDescriptorSetLayout(this.deviceContext.getDevice(),
                                                      createInfo,
                                                      null,
                                                      pLayout),
                          "Creating descriptor set layout failed");
            return pLayout.get(0);
        }
    }

    private long[] allocateDescriptorSets() {
        final var descriptorSets = new long[this.swapchain.getImageCount()];

        try (final var stack = stackPush()) {
            final var layouts = stack.mallocLong(this.swapchain.getImageCount());
            for (int i = 0; i < this.swapchain.getImageCount(); i++) {
                layouts.put(i, this.descriptorSetLayout);
            }
            final var allocateInfo = VkDescriptorSetAllocateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                    .descriptorPool(this.descriptorPool.getHandle())
                    .pSetLayouts(layouts);

            ensureSuccess(vkAllocateDescriptorSets(this.deviceContext.getDevice(),
                                                   allocateInfo,
                                                   descriptorSets),
                          "Allocating descriptor sets failed!");
        }

        return descriptorSets;
    }

    @Override
    protected void cleanup() {
        for (final var binding : this.bindings) {
            binding.close();
        }
        vkDestroyDescriptorSetLayout(this.deviceContext.getDevice(), this.descriptorSetLayout, null);
    }
}
