package fi.jakojaannos.roguelite.vulkan.descriptor;

import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;

import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;

import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class DescriptorSetLayout implements AutoCloseable {
    private final DeviceContext deviceContext;
    private final long handle;

    public long getHandle() {
        return this.handle;
    }

    public DescriptorSetLayout(
            final DeviceContext deviceContext,
            final DescriptorSetBinding... bindings
    ) {
        this.deviceContext = deviceContext;

        try (final var stack = stackPush()) {
            final var layoutBindings = VkDescriptorSetLayoutBinding.callocStack(bindings.length);
            for (int i = 0; i < bindings.length; i++) {
                final var binding = bindings[i];

                layoutBindings.get(i)
                              .binding(binding.slot())
                              .descriptorType(binding.descriptorType())
                              .descriptorCount(binding.descriptorCount())
                              .stageFlags(binding.stageFlags());
            }

            final var createInfo = VkDescriptorSetLayoutCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
                    .pBindings(layoutBindings);

            final var pLayout = stack.mallocLong(1);
            ensureSuccess(vkCreateDescriptorSetLayout(deviceContext.getDevice(),
                                                      createInfo,
                                                      null,
                                                      pLayout),
                          "Creating descriptor set layout failed");
            this.handle = pLayout.get(0);
        }
    }

    @Override
    public void close() {
        vkDestroyDescriptorSetLayout(this.deviceContext.getDevice(), this.handle, null);
    }
}
