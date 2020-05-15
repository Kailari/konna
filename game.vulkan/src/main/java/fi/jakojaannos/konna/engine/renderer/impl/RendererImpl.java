package fi.jakojaannos.konna.engine.renderer.impl;

import java.nio.file.Path;

import fi.jakojaannos.konna.engine.CameraUniformBufferObject;
import fi.jakojaannos.konna.engine.PresentableState;
import fi.jakojaannos.konna.engine.renderer.DebugRenderer;
import fi.jakojaannos.konna.engine.renderer.Renderer;
import fi.jakojaannos.konna.engine.util.RecreateCloseable;
import fi.jakojaannos.konna.engine.vulkan.DepthTexture;
import fi.jakojaannos.konna.engine.vulkan.command.CommandBuffer;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.konna.engine.vulkan.descriptor.SwapchainImageDependentDescriptorPool;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.engine.vulkan.rendering.Framebuffers;
import fi.jakojaannos.konna.engine.vulkan.rendering.RenderPass;
import fi.jakojaannos.konna.engine.vulkan.rendering.Swapchain;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class RendererImpl extends RecreateCloseable implements Renderer {
    private final DeviceContext deviceContext;
    private final Swapchain swapchain;

    private final DepthTexture depthTexture;
    private final RenderPass renderPass;
    private final Framebuffers framebuffers;
    private final DescriptorPool descriptorPool;

    private final DescriptorSetLayout cameraDescriptorLayout;
    private final CameraUniformBufferObject cameraUBO;

    private final DebugRendererImpl debugRenderer;

    private CommandBuffer[] commandBuffers;

    public CameraUniformBufferObject getCameraUBO() {
        return this.cameraUBO;
    }

    public void setWriteState(final PresentableState state) {
        this.debugRenderer.setWriteState(state);
    }

    public RendererImpl(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final Path assetRoot
    ) {
        this.deviceContext = deviceContext;
        this.swapchain = swapchain;

        this.depthTexture = new DepthTexture(deviceContext, this.swapchain);

        this.renderPass = new RenderPass(deviceContext, this.swapchain);
        this.framebuffers = new Framebuffers(deviceContext,
                                             this.swapchain,
                                             this.depthTexture,
                                             this.renderPass);

        // We have swapchainImageCount copies of n descriptor sets. Why use suppliers? That way we
        // can delay the descriptorCount/maxSets calculations to `tryRecreate`, where all resources
        // are already initialized. In other words: we do not yet know the image count here so use
        // suppliers to move the time of making the decision to a later point in time
        this.descriptorPool = new SwapchainImageDependentDescriptorPool(
                deviceContext,
                this.swapchain,
                2 + 7 + 1,
                new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                        () -> this.swapchain.getImageCount() * (2 + 7)),
                new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                                        this.swapchain::getImageCount));

        this.cameraDescriptorLayout = new DescriptorSetLayout(deviceContext,
                                                              CameraUniformBufferObject.CAMERA_DESCRIPTOR_BINDING);
        this.cameraUBO = new CameraUniformBufferObject(deviceContext,
                                                       swapchain,
                                                       this.descriptorPool,
                                                       this.cameraDescriptorLayout,
                                                       25.0f);

        this.debugRenderer = new DebugRendererImpl(deviceContext,
                                                   swapchain,
                                                   this.renderPass,
                                                   assetRoot,
                                                   this.cameraDescriptorLayout);
    }

    public void flush(
            final PresentableState presentableState,
            final int imageIndex
    ) {
        final var commandBuffer = this.commandBuffers[imageIndex];
        final var framebuffer = this.framebuffers.get(imageIndex);

        try (final var ignored = commandBuffer.begin();
             final var ignored2 = this.renderPass.begin(framebuffer, commandBuffer)
        ) {
            this.debugRenderer.setReadState(presentableState);
            this.debugRenderer.flush(this.cameraUBO, commandBuffer, imageIndex);
        }
    }

    public void submit(
            final int imageIndex,
            final long fence,
            final long imageAvailableSemaphore,
            final long renderFinishedSemaphore
    ) {
        this.deviceContext.getGraphicsQueue()
                          .submit(this.commandBuffers[imageIndex],
                                  fence,
                                  new long[]{imageAvailableSemaphore},
                                  new int[]{VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT},
                                  new long[]{renderFinishedSemaphore});
    }

    @Override
    public DebugRenderer debug() {
        return this.debugRenderer;
    }

    @Override
    protected void recreate() {
        freeCommandBuffers();
        this.commandBuffers = this.deviceContext.getGraphicsCommandPool()
                                                .allocate(this.swapchain.getImageCount());

        this.depthTexture.tryRecreate();
        this.renderPass.tryRecreate();
        this.framebuffers.tryRecreate();
        this.descriptorPool.tryRecreate();

        this.cameraUBO.tryRecreate();

        this.debugRenderer.tryRecreate();
    }

    private void freeCommandBuffers() {
        // In case command buffers haven't yet been initialized or are already cleaned up
        if (this.commandBuffers == null) {
            return;
        }

        try (final var stack = stackPush()) {
            final var pBuffers = stack.mallocPointer(this.commandBuffers.length);
            for (int i = 0; i < this.commandBuffers.length; i++) {
                pBuffers.put(i, this.commandBuffers[i].getHandle());
            }
            vkFreeCommandBuffers(this.deviceContext.getDevice(),
                                 this.deviceContext.getGraphicsCommandPool().getHandle(),
                                 pBuffers);
        }
    }

    @Override
    protected void cleanup() {
    }

    @Override
    public void close() {
        this.depthTexture.close();
        this.renderPass.close();
        this.framebuffers.close();
        this.descriptorPool.close();

        this.cameraUBO.close();

        this.debugRenderer.close();

        this.cameraDescriptorLayout.close();
        super.close();
    }
}
