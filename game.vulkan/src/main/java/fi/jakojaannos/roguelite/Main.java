package fi.jakojaannos.roguelite;

import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;

import static fi.jakojaannos.roguelite.util.VkUtil.translateVulkanResult;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static long imageAvailableSemaphore;
    private static long renderFinishedSemaphore;

    public static void main(final String[] args) {
        try (final var app = Application.initialize(800,
                                                    600,
                                                    Path.of("assets"))) {
            app.window().show();

            final var renderCommandBuffers = new RenderCommandBuffers(app.graphicsCommandPool(),
                                                                      app.swapchain().getImageCount(),
                                                                      app.renderPass(),
                                                                      app.framebuffers(),
                                                                      app.graphicsPipeline());
            imageAvailableSemaphore = createSemaphore(app.deviceContext());
            renderFinishedSemaphore = createSemaphore(app.deviceContext());

            try {
                while (app.window().isOpen()) {
                    app.window().handleOSEvents();

                    drawFrame(app, renderCommandBuffers);

                    vkQueueWaitIdle(app.deviceContext().getGraphicsQueue());
                    vkQueueWaitIdle(app.deviceContext().getPresentQueue());
                }
            } catch (final Throwable t) {
                LOG.error("Application has crashed: " + t.getMessage());
            }

            vkDeviceWaitIdle(app.deviceContext().getDevice());

            vkDestroySemaphore(app.deviceContext().getDevice(), imageAvailableSemaphore, null);
            vkDestroySemaphore(app.deviceContext().getDevice(), renderFinishedSemaphore, null);
        }
    }

    private static void drawFrame(final Application app, final RenderCommandBuffers renderCommandBuffers) {
        final var deviceContext = app.deviceContext();

        final int imageIndex;
        try (final var stack = stackPush()) {
            final var pImageIndex = stack.mallocInt(1);
            final var result = vkAcquireNextImageKHR(deviceContext.getDevice(),
                                                     app.swapchain().getHandle(),
                                                     -1L,
                                                     imageAvailableSemaphore,
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
                    .pWaitSemaphores(stack.longs(imageAvailableSemaphore))
                    .pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
                    .pCommandBuffers(stack.pointers(renderCommandBuffers.get(imageIndex).getHandle()))
                    .pSignalSemaphores(stack.longs(renderFinishedSemaphore));

            final var result = vkQueueSubmit(app.deviceContext().getGraphicsQueue(),
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
                    .pWaitSemaphores(stack.longs(renderFinishedSemaphore))
                    .swapchainCount(1)
                    .pSwapchains(stack.longs(app.swapchain().getHandle()))
                    .pImageIndices(stack.ints(imageIndex));
            final var result = vkQueuePresentKHR(app.deviceContext().getPresentQueue(), presentInfo);
            if (result != VK_SUCCESS) {
                //throw new IllegalStateException("Presenting swapchain image failed: "
                //                                + translateVulkanResult(result));
            }
        }
    }

    private static long createSemaphore(final DeviceContext deviceContext) {
        try (final var stack = stackPush()) {
            final var createInfo = VkSemaphoreCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

            final var pSemaphore = stack.mallocLong(1);
            final var result = vkCreateSemaphore(deviceContext.getDevice(), createInfo, null, pSemaphore);
            if (result != VK_SUCCESS) {
                throw new IllegalStateException("Creating semaphore failed: "
                                                + translateVulkanResult(result));
            }

            return pSemaphore.get(0);
        }
    }
}
