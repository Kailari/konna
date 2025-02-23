package fi.jakojaannos.riista.vulkan.rendering;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.nio.ByteBuffer;

import static fi.jakojaannos.riista.vulkan.util.VkUtil.translateVulkanResult;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class ShaderModule implements AutoCloseable {
    private final VkDevice device;
    private final long handle;

    public long getHandle() {
        return this.handle;
    }

    public ShaderModule(final VkDevice device, final ByteBuffer compiledSource) {
        this.device = device;

        try (final var stack = stackPush()) {
            final var createInfo = VkShaderModuleCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                    .pCode(compiledSource);

            final var pModule = stack.mallocLong(1);
            final var result = vkCreateShaderModule(this.device, createInfo, null, pModule);
            if (result != VK_SUCCESS) {
                throw new IllegalStateException("Creating shader module failed: "
                                                + translateVulkanResult(result));
            }
            this.handle = pModule.get(0);
        }
    }

    @Override
    public void close() {
        vkDestroyShaderModule(this.device, this.handle, null);
    }
}
