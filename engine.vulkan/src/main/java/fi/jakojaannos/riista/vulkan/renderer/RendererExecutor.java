package fi.jakojaannos.riista.vulkan.renderer;

import fi.jakojaannos.konna.engine.vulkan.application.PresentableState;
import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.konna.engine.util.RecreateCloseable;
import fi.jakojaannos.riista.vulkan.CameraDescriptor;
import fi.jakojaannos.riista.vulkan.renderer.debug.DebugRendererExecutor;
import fi.jakojaannos.riista.vulkan.renderer.mesh.MeshRendererExecutor;
import fi.jakojaannos.riista.vulkan.renderer.ui.UiRendererExecutor;
import fi.jakojaannos.konna.engine.vulkan.DepthTexture;
import fi.jakojaannos.konna.engine.vulkan.RenderingBackend;
import fi.jakojaannos.konna.engine.vulkan.command.CommandBuffer;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.konna.engine.vulkan.descriptor.SwapchainImageDependentDescriptorPool;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.riista.vulkan.rendering.Framebuffers;
import fi.jakojaannos.riista.vulkan.rendering.RenderPass;
import fi.jakojaannos.riista.vulkan.rendering.Swapchain;
import fi.jakojaannos.konna.engine.vulkan.types.VkDescriptorPoolCreateFlags;
import fi.jakojaannos.konna.engine.vulkan.window.Window;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;

import static fi.jakojaannos.riista.utilities.BitMask.bitMask;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class RendererExecutor extends RecreateCloseable {
    private final DeviceContext deviceContext;
    private final Swapchain swapchain;

    private final DepthTexture depthTexture;
    private final RenderPass renderPass;
    private final Framebuffers framebuffers;
    private final DescriptorPool descriptorPool;

    private final DescriptorSetLayout cameraDescriptorLayout;

    private final CameraDescriptor cameraDescriptor;

    private final DebugRendererExecutor debugRenderer;
    private final MeshRendererExecutor meshRenderer;
    private final UiRendererExecutor uiRenderer;

    private CommandBuffer[] commandBuffers;

    public RendererExecutor(
            final RenderingBackend backend,
            final Window window,
            final AssetManager assetManager
    ) {
        this.deviceContext = backend.deviceContext();
        this.swapchain = backend.swapchain();

        this.depthTexture = new DepthTexture(this.deviceContext, this.swapchain);

        this.renderPass = new RenderPass(this.deviceContext, this.swapchain);
        this.framebuffers = new Framebuffers(this.deviceContext,
                                             this.swapchain,
                                             this.depthTexture,
                                             this.renderPass);

        this.descriptorPool = new SwapchainImageDependentDescriptorPool(backend,
                                                                        1,
                                                                        bitMask(VkDescriptorPoolCreateFlags.FREE_DESCRIPTOR_SET_BIT),
                                                                        new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, this.swapchain::getImageCount));

        this.cameraDescriptorLayout = new DescriptorSetLayout(this.deviceContext,
                                                              CameraDescriptor.CAMERA_DESCRIPTOR_BINDING);

        this.cameraDescriptor = new CameraDescriptor(this.deviceContext,
                                                     this.swapchain,
                                                     this.descriptorPool,
                                                     this.cameraDescriptorLayout,
                                                     25.0f);

        this.debugRenderer = new DebugRendererExecutor(backend,
                                                       this.renderPass,
                                                       assetManager,
                                                       this.cameraDescriptorLayout);
        this.meshRenderer = new MeshRendererExecutor(backend,
                                                     this.renderPass,
                                                     assetManager,
                                                     this.cameraDescriptorLayout);
        this.uiRenderer = new UiRendererExecutor(backend,
                                                 window,
                                                 this.renderPass,
                                                 assetManager);

        tryRecreate();
    }

    public void updateCameraProperties(final CameraProperties cameraProperties) {
        final var realProjection = this.cameraDescriptor.getProjectionMatrix();
        cameraProperties.projection.set(realProjection);
        cameraProperties.inverseProjection.set(realProjection)
                                          .invert();
    }

    public void recordFrame(
            final PresentableState presentableState,
            final int imageIndex
    ) {
        final var viewMatrix = presentableState.viewMatrix();
        final var eyePosition = presentableState.eyePosition();
        this.cameraDescriptor.update(imageIndex, eyePosition, viewMatrix);

        final var commandBuffer = this.commandBuffers[imageIndex];
        final var framebuffer = this.framebuffers.get(imageIndex);

        try (final var ignored = commandBuffer.begin();
             final var ignored2 = this.renderPass.begin(framebuffer, commandBuffer)
        ) {
            this.debugRenderer.flush(presentableState, this.cameraDescriptor, commandBuffer, imageIndex);
            this.meshRenderer.flush(presentableState, this.cameraDescriptor, commandBuffer, imageIndex);
            this.uiRenderer.flush(presentableState, commandBuffer, imageIndex);
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
    protected void recreate() {
        freeCommandBuffers();
        this.commandBuffers = this.deviceContext.getGraphicsCommandPool()
                                                .allocate(this.swapchain.getImageCount());

        this.depthTexture.tryRecreate();
        this.renderPass.tryRecreate();
        this.framebuffers.tryRecreate();
        this.descriptorPool.tryRecreate();

        this.cameraDescriptor.tryRecreate();

        this.debugRenderer.tryRecreate();
        this.meshRenderer.tryRecreate();
        this.uiRenderer.tryRecreate();
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

        this.cameraDescriptor.close();

        this.debugRenderer.close();
        this.meshRenderer.close();
        this.uiRenderer.close();

        this.cameraDescriptorLayout.close();
        super.close();
    }
}
