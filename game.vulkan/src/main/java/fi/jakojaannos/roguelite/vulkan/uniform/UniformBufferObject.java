package fi.jakojaannos.roguelite.vulkan.uniform;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;

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
 * Each UBO contains one or more <strong>bindings</strong>, which each describe a separate uniform block/variable on the
 * shader. How these bindings are used is described by <i>descriptor set layout</i>. The layout can then be used within
 * a {@link fi.jakojaannos.roguelite.vulkan.rendering.GraphicsPipeline Graphics Pipeline} to tell the pipeline how to
 * use the uniforms.
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
    private final UniformBinding<?>[] bindings;

    private long descriptorSetLayout;
    private long oldSwapchainImageCount;

    @Override
    public boolean isRecreateRequired() {
        return this.oldSwapchainImageCount != this.swapchain.getImageCount();
    }

    public long getLayoutHandle() {
        return this.descriptorSetLayout;
    }

    public UniformBufferObject(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final UniformBinding<?>... bindings
    ) {
        this.deviceContext = deviceContext;
        this.swapchain = swapchain;
        this.bindings = bindings;

        tryRecreate();
    }

    @Override
    protected void recreate() {
        this.oldSwapchainImageCount = this.swapchain.getImageCount();
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
            this.descriptorSetLayout = pLayout.get(0);
        }
    }

    @Override
    protected void cleanup() {
        for (final var binding : this.bindings) {
            binding.close();
        }
        vkDestroyDescriptorSetLayout(this.deviceContext.getDevice(), this.descriptorSetLayout, null);
    }

    public GPUBuffer getBindingBuffer(final int binding, final int imageIndex) {
        return this.bindings[binding].getBuffer(imageIndex);
    }
}
