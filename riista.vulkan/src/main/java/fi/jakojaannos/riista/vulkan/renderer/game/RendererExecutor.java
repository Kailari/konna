package fi.jakojaannos.riista.vulkan.renderer.game;

import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.vulkan.CameraDescriptor;
import fi.jakojaannos.riista.vulkan.application.PresentableState;
import fi.jakojaannos.riista.vulkan.internal.DepthTexture;
import fi.jakojaannos.riista.vulkan.internal.RenderingBackend;
import fi.jakojaannos.riista.vulkan.internal.command.CommandBuffer;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorPool;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorSetLayout;
import fi.jakojaannos.riista.vulkan.internal.descriptor.SwapchainImageDependentDescriptorPool;
import fi.jakojaannos.riista.vulkan.internal.device.DeviceContext;
import fi.jakojaannos.riista.vulkan.internal.types.VkAccessFlagBits;
import fi.jakojaannos.riista.vulkan.internal.types.VkDescriptorPoolCreateFlags;
import fi.jakojaannos.riista.vulkan.internal.types.VkPipelineStageFlagBits;
import fi.jakojaannos.riista.vulkan.internal.window.Window;
import fi.jakojaannos.riista.vulkan.renderer.debug.DebugRendererExecutor;
import fi.jakojaannos.riista.vulkan.renderer.mesh.MeshRendererExecutor;
import fi.jakojaannos.riista.vulkan.renderer.ui.UiRendererExecutor;
import fi.jakojaannos.riista.vulkan.rendering.*;
import fi.jakojaannos.riista.vulkan.util.RecreateCloseable;
import fi.jakojaannos.riista.data.resources.CameraProperties;

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

        final var colorAttachment = new PresentColorAttachment(this.swapchain);
        final var mainRenderPass = RenderSubpass.builder()
                                                .colorAttachments(colorAttachment)
                                                .withDepthAttachment()
                                                .build();
        final var uiRenderPass = RenderSubpass.builder()
                                              .colorAttachments(colorAttachment)
                                              .build();
        this.renderPass = RenderPass.builder(backend)
                                    .colorAttachment(0, colorAttachment)
                                    .withDepthAttachment(1)
                                    .subpass(mainRenderPass)
                                    .subpass(uiRenderPass)
                                    // Makes sure we do not try to start writing until any previous frames have finished
                                    // rendering to the attachment. The "external pass" here effectively means "any
                                    // previously submitted commands"
                                    .subpassDependency(VK_SUBPASS_EXTERNAL,
                                                       mainRenderPass,
                                                       bitMask(VkPipelineStageFlagBits.COLOR_ATTACHMENT_OUTPUT_BIT),
                                                       bitMask(),
                                                       bitMask(VkPipelineStageFlagBits.COLOR_ATTACHMENT_OUTPUT_BIT),
                                                       bitMask(VkAccessFlagBits.COLOR_ATTACHMENT_WRITE_BIT))
                                    // Make UI pass depend on main. What we would like to achieve is to get UI always
                                    // render on top of the main render pass.
                                    //
                                    // Naive approach would be to prevent the UI from starting the rendering until
                                    // the main pass has finished, but that is very inefficient.
                                    //
                                    // Instead, we can run UI rendering in parallel with the main pass and only prevent
                                    // the UI pass from writing to the color attachment until the main pass finishes.
                                    // This way, everything but writing to the color attachment can be prepared while
                                    // waiting for the main pass to finish and when the main pass finishes, we just
                                    // output the color data on-screen.
                                    //
                                    // This works because the only shared GPU resource between the two is the color
                                    // attachment.
                                    .subpassDependency(mainRenderPass,
                                                       uiRenderPass,
                                                       bitMask(VkPipelineStageFlagBits.COLOR_ATTACHMENT_OUTPUT_BIT),
                                                       bitMask(),
                                                       bitMask(VkPipelineStageFlagBits.COLOR_ATTACHMENT_OUTPUT_BIT),
                                                       bitMask(VkAccessFlagBits.COLOR_ATTACHMENT_WRITE_BIT))
                                    .build();
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
                                                       mainRenderPass,
                                                       assetManager,
                                                       this.cameraDescriptorLayout);
        this.meshRenderer = new MeshRendererExecutor(backend,
                                                     this.renderPass,
                                                     mainRenderPass,
                                                     assetManager,
                                                     this.cameraDescriptorLayout);
        this.uiRenderer = new UiRendererExecutor(backend,
                                                 window,
                                                 this.renderPass,
                                                 uiRenderPass,
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

            vkCmdNextSubpass(commandBuffer.getHandle(), VK_SUBPASS_CONTENTS_INLINE);
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
