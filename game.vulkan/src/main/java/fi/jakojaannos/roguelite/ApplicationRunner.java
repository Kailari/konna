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

    private static final double DEGREES_PER_SECOND = 33.0;
    private static final double RADIANS_PER_SECOND = Math.toRadians(DEGREES_PER_SECOND);

    private final long[] imageAvailableSemaphores;
    private final long[] renderFinishedSemaphores;
    private final long[] inFlightFences;

    private final long[] imagesInFlight;

    private final Application application;

    private boolean framebufferResized;
    private int frameIndex;

    private double angle;

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
        this.imagesInFlight = new long[application.renderer().getSwapchainImageCount()];
        Arrays.fill(this.imagesInFlight, VK_NULL_HANDLE);

        this.application.window()
                        .onResize((width, height) -> this.framebufferResized = true);
    }

    public void run() {
        this.application.window().show();
        this.frameIndex = -1;

        try {
            var timestamp = System.currentTimeMillis();
            while (this.application.window().isOpen()) {
                final var currentTime = System.currentTimeMillis();
                final var delta = (currentTime - timestamp) / 1000.0;
                timestamp = currentTime;

                this.application.window().handleOSEvents();

                // Proceed to the next frame and wait until the frame fence is free. This prevents
                // CPU from pushing more frames than what GPU can handle.
                this.frameIndex = (this.frameIndex + 1) % MAX_FRAMES_IN_FLIGHT;
                vkWaitForFences(this.application.backend().deviceContext().getDevice(),
                                this.inFlightFences[this.frameIndex],
                                true,
                                -1L);

                final int imageIndex = acquireNextImage();
                if (imageIndex == -1) {
                    continue;
                }

                drawFrame(delta, this.application.renderer().getCommands(imageIndex), imageIndex);
                presentImage(imageIndex);
            }
        } catch (final Throwable t) {
            LOG.error("Application has crashed: " + t);
        }
    }

    private int acquireNextImage() {
        final var deviceContext = this.application.backend().deviceContext();

        final int imageIndex;
        try (final var stack = stackPush()) {
            final var pImageIndex = stack.mallocInt(1);
            final var result = vkAcquireNextImageKHR(deviceContext.getDevice(),
                                                     this.application.renderer().getSwapchain().getHandle(),
                                                     -1L,
                                                     this.imageAvailableSemaphores[this.frameIndex],
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

        // Wait until the image has finished rendering. This ensures that the resources associated
        // with the image are free to use when we start recording/submitting the command buffers etc.
        if (this.imagesInFlight[imageIndex] != VK_NULL_HANDLE) {
            vkWaitForFences(deviceContext.getDevice(), this.imagesInFlight[imageIndex], true, -1L);
        }
        this.imagesInFlight[imageIndex] = this.inFlightFences[this.frameIndex];

        return imageIndex;
    }

    private void drawFrame(final double delta, final CommandBuffer commandBuffer, final int imageIndex) {
        this.angle += delta * RADIANS_PER_SECOND;
        this.application.renderer()
                        .getCameraUBO()
                        .update(imageIndex, this.angle);

        this.application.backend()
                        .deviceContext()
                        .getGraphicsQueue()
                        .submit(commandBuffer,
                                this.inFlightFences[this.frameIndex],
                                new long[]{this.imageAvailableSemaphores[this.frameIndex]},
                                new int[]{VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT},
                                new long[]{this.renderFinishedSemaphores[this.frameIndex]});
    }

    private void presentImage(final int imageIndex) {
        try (final var stack = stackPush()) {
            final var presentInfo = VkPresentInfoKHR
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                    .pWaitSemaphores(stack.longs(this.renderFinishedSemaphores[this.frameIndex]))
                    .swapchainCount(1)
                    .pSwapchains(stack.longs(this.application.renderer().getSwapchain().getHandle()))
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

    private void recreateSwapchain() {
        waitUntilNotMinimized();

        vkDeviceWaitIdle(this.application.backend().deviceContext().getDevice());
        LOG.debug("Recreating swapchain!");
        this.framebufferResized = false;

        this.application.recreateSwapchain();
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
