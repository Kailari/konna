package fi.jakojaannos.roguelite;

import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;

import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
import static fi.jakojaannos.roguelite.util.VkUtil.translateVulkanResult;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public class ApplicationRunner implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationRunner.class);

    private final long imageAvailableSemaphore;
    private final long renderFinishedSemaphore;

    private final Application application;

    public ApplicationRunner(final Application application) {
        this.application = application;

        final var deviceContext = application.backend().deviceContext();
        this.imageAvailableSemaphore = createSemaphore(deviceContext);
        this.renderFinishedSemaphore = createSemaphore(deviceContext);
    }

    public void run() {
        final var commands = new RenderCommandBuffers(this.application.graphicsCommandPool(),
                                                      this.application.swapchain().getImageCount(),
                                                      this.application.renderPass(),
                                                      this.application.framebuffers(),
                                                      this.application.graphicsPipeline());

        this.application.window().show();

        try {
            final var deviceContext = this.application.backend().deviceContext();

            while (this.application.window().isOpen()) {
                this.application.window().handleOSEvents();

                drawFrame(commands);

                vkQueueWaitIdle(deviceContext.getGraphicsQueue());
                vkQueueWaitIdle(deviceContext.getPresentQueue());
            }
        } catch (final Throwable t) {
            LOG.error("Application has crashed: " + t.getMessage());
        }
    }

    private void drawFrame(final RenderCommandBuffers renderCommandBuffers) {
        final var app = this.application;
        final var deviceContext = app.backend().deviceContext();

        final int imageIndex;
        try (final var stack = stackPush()) {
            final var pImageIndex = stack.mallocInt(1);
            final var result = vkAcquireNextImageKHR(deviceContext.getDevice(),
                                                     app.swapchain().getHandle(),
                                                     -1L,
                                                     this.imageAvailableSemaphore,
                                                     VK_NULL_HANDLE,
                                                     pImageIndex);
            if (result != VK_SUCCESS) {
                throw new IllegalStateException("Acquiring swapchain image failed: "
                                                + translateVulkanResult(result));
            }
            imageIndex = pImageIndex.get(0);
        }

        try (final var stack = stackPush()) {
            final var submitInfo = VkSubmitInfo
                    .callocStack(1)
                    .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                    .waitSemaphoreCount(1)
                    .pWaitSemaphores(stack.longs(this.imageAvailableSemaphore))
                    .pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
                    .pCommandBuffers(stack.pointers(renderCommandBuffers.get(imageIndex).getHandle()))
                    .pSignalSemaphores(stack.longs(this.renderFinishedSemaphore));

            final var result = vkQueueSubmit(deviceContext.getGraphicsQueue(),
                                             submitInfo,
                                             VK_NULL_HANDLE);
            if (result != VK_SUCCESS) {
                throw new IllegalStateException("Queue submit failed: "
                                                + translateVulkanResult(result));
            }
        }

        try (final var stack = stackPush()) {
            final var presentInfo = VkPresentInfoKHR
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                    .pWaitSemaphores(stack.longs(this.renderFinishedSemaphore))
                    .swapchainCount(1)
                    .pSwapchains(stack.longs(app.swapchain().getHandle()))
                    .pImageIndices(stack.ints(imageIndex));
            final var result = vkQueuePresentKHR(deviceContext.getPresentQueue(), presentInfo);
            if (result != VK_SUCCESS) {
                throw new IllegalStateException("Presenting swapchain image failed: "
                                                + translateVulkanResult(result));
            }
        }
    }

    @Override
    public void close() {
        final var deviceContext = this.application.backend().deviceContext();
        vkDeviceWaitIdle(deviceContext.getDevice());

        vkDestroySemaphore(deviceContext.getDevice(), this.imageAvailableSemaphore, null);
        vkDestroySemaphore(deviceContext.getDevice(), this.renderFinishedSemaphore, null);
    }

    private static long createSemaphore(final DeviceContext deviceContext) {
        try (final var stack = stackPush()) {
            final var createInfo = VkSemaphoreCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

            final var pSemaphore = stack.mallocLong(1);
            ensureSuccess(vkCreateSemaphore(deviceContext.getDevice(), createInfo, null, pSemaphore),
                          "Creating semaphore failed");

            return pSemaphore.get(0);
        }
    }
}
