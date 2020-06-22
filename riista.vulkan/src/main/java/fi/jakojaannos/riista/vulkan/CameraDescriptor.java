package fi.jakojaannos.riista.vulkan;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.vulkan.VkExtent2D;

import java.nio.ByteBuffer;

import fi.jakojaannos.riista.vulkan.internal.descriptor.*;
import fi.jakojaannos.riista.vulkan.internal.device.DeviceContext;
import fi.jakojaannos.riista.vulkan.rendering.Swapchain;

import static org.lwjgl.vulkan.VK10.*;

public class CameraDescriptor extends DescriptorObject {
    public static final DescriptorBinding CAMERA_DESCRIPTOR_BINDING = new DescriptorBinding(0,
                                                                                            VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                                                            1,
                                                                                            VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);

    private final CameraMatrices cameraMatrices;

    private final VkExtent2D oldSwapchainExtent;
    private final Swapchain swapchain;

    @Override
    protected boolean isRecreateRequired() {
        return super.isRecreateRequired() || widthHasChanged() || heightHasChanged();
    }

    public Matrix4f getProjectionMatrix() {
        return this.cameraMatrices.projection;
    }

    public CameraDescriptor(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout
    ) {
        this(deviceContext,
             swapchain,
             descriptorPool,
             layout,
             new CameraMatrices());
    }

    private CameraDescriptor(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout,
            final CameraMatrices cameraMatrices
    ) {
        super(deviceContext,
              swapchain::getImageCount,
              descriptorPool,
              layout,
              new CombinedImageSamplerBinding[0],
              new UniformBufferBinding[]{cameraMatrices});

        this.swapchain = swapchain;
        this.oldSwapchainExtent = VkExtent2D.calloc();
        this.cameraMatrices = cameraMatrices;
    }

    private boolean widthHasChanged() {
        return this.oldSwapchainExtent.width() != this.swapchain.getExtent().width();
    }

    private boolean heightHasChanged() {
        return this.oldSwapchainExtent.height() != this.swapchain.getExtent().height();
    }

    @Override
    protected void recreate() {
        this.oldSwapchainExtent.set(this.swapchain.getExtent());
        super.recreate();
    }

    @Override
    public void close() {
        super.close();
        this.oldSwapchainExtent.free();
    }

    public void update(final int imageIndex, final Vector3f position, final Matrix4f view) {
        final var swapchainExtent = this.swapchain.getExtent();
        final var aspectRatio = swapchainExtent.width() / (float) swapchainExtent.height();

        // Create right-handed perspective projection matrix with Y-axis flipped
        // (Vulkan NDC has Y pointing down which we need to correct with projection matrix)
        //
        // This results in coordinate system where:
        //  -X: Left       +X: Right
        //  -Y: Back       +Y: Forward
        //  -Z: Down       +Z: Up
        this.cameraMatrices.projection.identity()
                                      .scale(1, -1, 1)
                                      .perspective((float) Math.toRadians(45.0),
                                                   aspectRatio,
                                                   0.1f, 10_000.0f, true);

        this.cameraMatrices.view.set(view);
        this.cameraMatrices.eyePosition.set(position);

        flushAllUniformBufferBindings(imageIndex);
    }

    private static class CameraMatrices implements UniformBufferBinding {
        private static final int OFFSET_VIEW = 0;
        private static final int OFFSET_PROJECTION = 16 * Float.BYTES;
        private static final int OFFSET_EYE_POSITION = 2 * 16 * Float.BYTES;

        private final Matrix4f view = new Matrix4f().identity();
        private final Matrix4f projection = new Matrix4f().identity();
        private final Vector3f eyePosition = new Vector3f();

        @Override
        public int binding() {
            return 0;
        }

        @Override
        public long sizeInBytes() {
            return 2 * 16 * Float.BYTES + 3 * Float.BYTES;
        }

        @Override
        public void write(final int offset, final ByteBuffer buffer) {
            this.view.get(offset + OFFSET_VIEW, buffer);
            this.projection.get(offset + OFFSET_PROJECTION, buffer);
            this.eyePosition.get(offset + OFFSET_EYE_POSITION, buffer);
        }
    }
}
