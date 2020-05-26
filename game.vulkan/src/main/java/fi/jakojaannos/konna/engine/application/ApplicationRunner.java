package fi.jakojaannos.konna.engine.application;

import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;

import fi.jakojaannos.konna.engine.assets.AssetManager;
import fi.jakojaannos.konna.engine.view.renderer.RendererExecutor;
import fi.jakojaannos.konna.engine.view.renderer.RendererRecorder;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameRunnerTimeManager;
import fi.jakojaannos.roguelite.engine.input.InputProvider;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

import static fi.jakojaannos.konna.engine.util.VkUtil.ensureSuccess;
import static fi.jakojaannos.konna.engine.util.VkUtil.translateVulkanResult;
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
    private final AssetManager assetManager;

    private final RendererExecutor renderer;

    private final GameRunnerTimeManager timeManager;

    private boolean framebufferResized;
    private int frameIndex;

    public TimeManager getTimeManager() {
        return this.timeManager;
    }

    public ApplicationRunner(final Application application, final AssetManager assetManager) {
        this.application = application;
        this.assetManager = assetManager;

        final var deviceContext = application.backend().deviceContext();
        final var swapchain = application.backend().swapchain();
        this.renderer = new RendererExecutor(application.backend(), assetManager);

        this.timeManager = new GameRunnerTimeManager(20L);

        this.imageAvailableSemaphores = new long[MAX_FRAMES_IN_FLIGHT];
        this.renderFinishedSemaphores = new long[MAX_FRAMES_IN_FLIGHT];
        this.inFlightFences = new long[MAX_FRAMES_IN_FLIGHT];
        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
            this.imageAvailableSemaphores[i] = createSemaphore(deviceContext);
            this.renderFinishedSemaphores[i] = createSemaphore(deviceContext);

            this.inFlightFences[i] = createFence(deviceContext);
        }
        this.imagesInFlight = new long[swapchain.getImageCount()];
        Arrays.fill(this.imagesInFlight, VK_NULL_HANDLE);

        this.application.window()
                        .onResize((width, height) -> this.framebufferResized = true);
    }

    public void run(final InputProvider inputProvider, final GameMode initialGameMode) {
        final var startTime = System.currentTimeMillis();

        var frameCounter = 0;
        try (final var simulationRunner = new SimulationThread(this.assetManager,
                                                               initialGameMode,
                                                               "riista-tick-thread",
                                                               inputProvider,
                                                               this.timeManager,
                                                               new RendererRecorder(this.application.backend().swapchain()::getExtent),
                                                               this.application.window()::setShouldClose,
                                                               this.renderer::updateCameraProperties)
        ) {
            this.application.window().show();
            this.frameIndex = -1;

            var timestamp = System.currentTimeMillis();
            final var deltaBuffer = new double[120];
            var gameModeId = -1;
            while (this.application.window().isOpen()) {
                final var currentTime = System.currentTimeMillis();
                final var delta = (currentTime - timestamp) / 1000.0;
                timestamp = currentTime;

                this.application.window().handleOSEvents();

                // Proceed to the next frame and wait until the frame fence is free. This prevents
                // CPU from pushing more frames than what the GPU can handle.
                this.frameIndex = (this.frameIndex + 1) % MAX_FRAMES_IN_FLIGHT;
                vkWaitForFences(this.application.backend().deviceContext().getDevice(),
                                this.inFlightFences[this.frameIndex],
                                true,
                                -1L);

                final int imageIndex = acquireNextImage();
                if (imageIndex == -1) {
                    continue;
                }

                final var state = simulationRunner.fetchPresentableState();

                final var deltaIndex = frameCounter % deltaBuffer.length;
                deltaBuffer[deltaIndex] = delta;
                if (deltaIndex == 0 && frameCounter > 0) {
                    // FIXME: Print to debug UI (write the avg. to presentable state like the rest of the UI)
                    LOG.debug("Average FPS for last {} frames: {}",
                              deltaBuffer.length,
                              String.format("%.2f", deltaBuffer.length / Arrays.stream(deltaBuffer).sum()));
                }

                if (state.gameModeId() != gameModeId) {
                    gameModeId = state.gameModeId();
                    onGameModeChanged(gameModeId);
                }
                this.renderer.recordFrame(state, imageIndex);
                this.renderer.submit(imageIndex,
                                     this.inFlightFences[this.frameIndex],
                                     this.imageAvailableSemaphores[this.frameIndex],
                                     this.renderFinishedSemaphores[this.frameIndex]);

                presentImage(imageIndex);
                ++frameCounter;
            }
        } catch (final Throwable t) {
            LOG.error("Application has crashed:");
            LOG.error("\tException:\t{}", t.getClass().getName());
            LOG.error("\tAt:\t\t{}:{}", t.getStackTrace()[0].getFileName(), t.getStackTrace()[0].getLineNumber());
            LOG.error("\tCause:\t\t{}", Objects.requireNonNullElse(t.getCause(), "Cause not defined."));
            LOG.error("\tMessage:\t{}", t.getMessage());

            LOG.error("\tStackTrace:\n{}",
                      Arrays.stream(t.getStackTrace())
                            .map(StackTraceElement::toString)
                            .reduce(t.toString(),
                                    (accumulator, element) -> String.format("%s\n\t%s", accumulator, element)));
        }

        final var totalTime = System.currentTimeMillis() - startTime;
        final var totalTimeSeconds = totalTime / 1000.0;

        final var avgTimePerTick = totalTime / (double) this.timeManager.getCurrentGameTime();
        final var avgTicksPerSecond = this.timeManager.getCurrentGameTime() / totalTimeSeconds;

        final var avgTimePerFrame = totalTime / (double) frameCounter;
        final var avgFramesPerSecond = frameCounter / totalTimeSeconds;
        LOG.info("Finished execution after {} seconds", totalTimeSeconds);
        LOG.info("\tTicks:\t\t{}", this.timeManager.getCurrentGameTime());
        LOG.info("\tAvg. TPT:\t{}", avgTimePerTick);
        LOG.info("\tAvg. TPS:\t{}", avgTicksPerSecond);
        LOG.info("\tFrames:\t\t{}", frameCounter);
        LOG.info("\tAvg. TPF:\t{}", avgTimePerFrame);
        LOG.info("\tAvg. FPS:\t{}", avgFramesPerSecond);
    }

    private void onGameModeChanged(final int gameModeId) {

    }

    private int acquireNextImage() {
        final var deviceContext = this.application.backend().deviceContext();

        final int imageIndex;
        try (final var stack = stackPush()) {
            final var pImageIndex = stack.mallocInt(1);
            final var result = vkAcquireNextImageKHR(deviceContext.getDevice(),
                                                     this.application.backend().swapchain().getHandle(),
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

    private void presentImage(final int imageIndex) {
        try (final var stack = stackPush()) {
            final var presentInfo = VkPresentInfoKHR
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                    .pWaitSemaphores(stack.longs(this.renderFinishedSemaphores[this.frameIndex]))
                    .swapchainCount(1)
                    .pSwapchains(stack.longs(this.application.backend().swapchain().getHandle()))
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

        this.application.backend().tryRecreate();
        this.renderer.tryRecreate();
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

        this.renderer.close();
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
