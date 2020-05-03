package fi.jakojaannos.roguelite;

import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;

import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
import static fi.jakojaannos.roguelite.util.VkUtil.translateVulkanResult;
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
    }

    public void run() {
        final var commands = new RenderCommandBuffers(this.application.graphicsCommandPool(),
                                                      this.application.swapchain().getImageCount(),
                                                      this.application.renderPass(),
                                                      this.application.framebuffers(),
                                                      this.application.graphicsPipeline());

        this.application.window().show();

        try {
            var currentFrame = 0;
            while (this.application.window().isOpen()) {
                this.application.window().handleOSEvents();

                drawFrame(commands, currentFrame);

                ++currentFrame;
            }
        } catch (final Throwable t) {
            LOG.error("Application has crashed: " + t.getMessage());
        }
    }

    private void drawFrame(final RenderCommandBuffers renderCommandBuffers, final int currentFrame) {
        final var app = this.application;
        final var deviceContext = app.backend().deviceContext();

        final var syncIndex = currentFrame % MAX_FRAMES_IN_FLIGHT;

        final var imageAvailableSemaphore = this.imageAvailableSemaphores[syncIndex];
        final var renderFinishedSemaphore = this.renderFinishedSemaphores[syncIndex];

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

        if (this.imagesInFlight[imageIndex] != VK_NULL_HANDLE) {
            vkWaitForFences(deviceContext.getDevice(), this.imagesInFlight[imageIndex], true, -1L);
        }
        this.imagesInFlight[imageIndex] = this.inFlightFences[syncIndex];

        try (final var stack = stackPush()) {
            final var submitInfo = VkSubmitInfo
                    .callocStack(1)
                    .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                    .waitSemaphoreCount(1)
                    .pWaitSemaphores(stack.longs(imageAvailableSemaphore))
                    .pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
                    .pCommandBuffers(stack.pointers(renderCommandBuffers.get(imageIndex).getHandle()))
                    .pSignalSemaphores(stack.longs(renderFinishedSemaphore));

            vkResetFences(deviceContext.getDevice(), this.inFlightFences[syncIndex]);
            ensureSuccess(vkQueueSubmit(deviceContext.getGraphicsQueue(),
                                        submitInfo,
                                        this.inFlightFences[syncIndex]),
                          "Rendering command submit failed");
        }

        try (final var stack = stackPush()) {
            final var presentInfo = VkPresentInfoKHR
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                    .pWaitSemaphores(stack.longs(renderFinishedSemaphore))
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
