package fi.jakojaannos.roguelite;

import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import fi.jakojaannos.roguelite.vulkan.command.CommandBuffer;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;

import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
import static fi.jakojaannos.roguelite.util.VkUtil.translateVulkanResult;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public class ApplicationRunner implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationRunner.class);
    private static final int MAX_FRAMES_IN_FLIGHT = 2;

    private final long[] imageAvailableSemaphores;
    private final long[] renderFinishedSemaphores;
    private final long[] inFlightFences;

    private final long[] imagesInFlight;

    private final Application application;

    private boolean framebufferResized;

    private RenderCommandBuffers commands;

    public ApplicationRunner(final Application application) {
        this.application = application;

        final var deviceContext = application.backend().deviceContext();
        this.imageAvailableSemaphores = new long[MAX_FRAMES_IN_FLIGHT];
        this.renderFinishedSemaphores = new long[MAX_FRAMES_IN_FLIGHT];
        this.inFlightFences = new long[MAX_FRAMES_IN_FLIGHT];
        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
            this.imageAvailableSemaphores[i] = createSemaphore(deviceContext);
            this.renderFinishedSemaphores[i] = createSemaphore(deviceContext);

            this.inFlightFences[i] = createFence(deviceContext);
        }
        this.imagesInFlight = new long[application.swapchain().getImageCount()];
        Arrays.fill(this.imagesInFlight, VK_NULL_HANDLE);

        this.commands = new RenderCommandBuffers(this.application.graphicsCommandPool(),
                                                 this.application.swapchain().getImageCount(),
                                                 this.application.renderPass(),
                                                 this.application.framebuffers(),
                                                 this.application.graphicsPipeline());

        this.application.window()
                        .onResize((width, height) -> this.framebufferResized = true);
    }

    public void run() {
        this.application.window().show();

        try {
            var currentFrame = 0;
            while (this.application.window().isOpen()) {
                this.application.window().handleOSEvents();

                if (drawFrame(this.commands, currentFrame)) {
                    continue;
                }

                ++currentFrame;
            }
        } catch (final Throwable t) {
            LOG.error("Application has crashed: " + t.getMessage());
        }
    }

    /**
     * Draws the next frame.
     *
     * @param renderCommandBuffers command buffers to use for rendering
     * @param currentFrame         current frame
     *
     * @return <code>false</code> if the frame was rendered, <code>true</code> if swapchain required re-creation before
     *         rendering the frame
     */
    private boolean drawFrame(final RenderCommandBuffers renderCommandBuffers, final int currentFrame) {
        final var syncIndex = currentFrame % MAX_FRAMES_IN_FLIGHT;
        vkWaitForFences(this.application.backend().deviceContext().getDevice(),
                        this.inFlightFences[syncIndex],
                        true,
                        -1L);

        final int imageIndex = acquireNextImage(syncIndex);
        if (imageIndex == -1) {
            return true;
        }

        submitDrawCommands(renderCommandBuffers.get(imageIndex), syncIndex);
        presentImage(syncIndex, imageIndex);

        return false;
    }

    private int acquireNextImage(final int syncIndex) {
        final var deviceContext = this.application.backend().deviceContext();

        final int imageIndex;
        try (final var stack = stackPush()) {
            final var pImageIndex = stack.mallocInt(1);
            final var result = vkAcquireNextImageKHR(deviceContext.getDevice(),
                                                     this.application.swapchain().getHandle(),
                                                     -1L,
                                                     this.imageAvailableSemaphores[syncIndex],
                                                     VK_NULL_HANDLE,
                                                     pImageIndex);
            if (result == VK_ERROR_OUT_OF_DATE_KHR) {
                recreateSwapchain();
                return -1;
            } else if (result != VK_SUCCESS && result != VK_SUBOPTIMAL_KHR) {
                throw new IllegalStateException("Acquiring swapchain image failed: " + translateVulkanResult(result));
            }
            imageIndex = pImageIndex.get(0);
        }

        if (this.imagesInFlight[imageIndex] != VK_NULL_HANDLE) {
            vkWaitForFences(deviceContext.getDevice(), this.imagesInFlight[imageIndex], true, -1L);
        }
        this.imagesInFlight[imageIndex] = this.inFlightFences[syncIndex];
        return imageIndex;
    }

    private void submitDrawCommands(final CommandBuffer commandBuffer, final int syncIndex) {
        final var deviceContext = this.application.backend().deviceContext();
        final var imageAvailableSemaphore = this.imageAvailableSemaphores[syncIndex];
        final var renderFinishedSemaphore = this.renderFinishedSemaphores[syncIndex];
        final var inFlightFence = this.inFlightFences[syncIndex];

        deviceContext.getGraphicsQueue()
                     .submit(commandBuffer,
                             inFlightFence,
                             new long[]{imageAvailableSemaphore},
                             new int[]{VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT},
                             new long[]{renderFinishedSemaphore});
    }

    private void presentImage(
            final int syncIndex,
            final int imageIndex
    ) {
        try (final var stack = stackPush()) {
            final var presentInfo = VkPresentInfoKHR
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                    .pWaitSemaphores(stack.longs(this.renderFinishedSemaphores[syncIndex]))
                    .swapchainCount(1)
                    .pSwapchains(stack.longs(this.application.swapchain().getHandle()))
                    .pImageIndices(stack.ints(imageIndex));

            final var deviceContext = this.application.backend().deviceContext();
            final var result = vkQueuePresentKHR(deviceContext.getPresentQueue().getHandle(),
                                                 presentInfo);
            if (result == VK_ERROR_OUT_OF_DATE_KHR || result == VK_SUBOPTIMAL_KHR || this.framebufferResized) {
                recreateSwapchain();
            } else if (result != VK_SUCCESS) {
                throw new IllegalStateException("Presenting swapchain image failed: "
                                                + translateVulkanResult(result));
            }
        }
    }

    public void recreateSwapchain() {
        waitUntilNotMinimized();

        vkDeviceWaitIdle(this.application.backend().deviceContext().getDevice());
        LOG.debug("Recreating swapchain!");
        this.framebufferResized = false;

        this.application.recreateSwapchain();

        this.commands.close();
        this.commands = new RenderCommandBuffers(this.application.graphicsCommandPool(),
                                                 this.application.swapchain().getImageCount(),
                                                 this.application.renderPass(),
                                                 this.application.framebuffers(),
                                                 this.application.graphicsPipeline());
    }

    private void waitUntilNotMinimized() {
        try (final var stack = stackPush()) {
            final var pWidth = stack.mallocInt(1);
            final var pHeight = stack.mallocInt(1);

            glfwGetFramebufferSize(this.application.window().getHandle(), pWidth, pHeight);
            // Loop until framebuffer area is non-zero
            while (pWidth.get(0) == 0 || pHeight.get(0) == 0) {
                glfwGetFramebufferSize(this.application.window().getHandle(), pWidth, pHeight);

                // Block until any window event occurs
                glfwWaitEvents();
            }
        }
    }

    @Override
    public void close() {
        final var deviceContext = this.application.backend().deviceContext();
        vkDeviceWaitIdle(deviceContext.getDevice());

        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
            vkDestroySemaphore(deviceContext.getDevice(), this.imageAvailableSemaphores[i], null);
            vkDestroySemaphore(deviceContext.getDevice(), this.renderFinishedSemaphores[i], null);
            vkDestroyFence(deviceContext.getDevice(), this.inFlightFences[i], null);
        }
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

    private static long createFence(final DeviceContext deviceContext) {
        try (final var stack = stackPush()) {
            final var createInfo = VkFenceCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
                    .flags(VK_FENCE_CREATE_SIGNALED_BIT);

            final var pFence = stack.mallocLong(1);
            ensureSuccess(vkCreateFence(deviceContext.getDevice(), createInfo, null, pFence),
                          "Creating fence failed");

            return pFence.get(0);
        }
    }
}
