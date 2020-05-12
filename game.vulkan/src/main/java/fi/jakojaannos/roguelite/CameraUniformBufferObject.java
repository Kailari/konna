package fi.jakojaannos.roguelite;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorBinding;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;
import fi.jakojaannos.roguelite.vulkan.uniform.CombinedImageSamplerBinding;
import fi.jakojaannos.roguelite.vulkan.uniform.DescriptorObject;
import fi.jakojaannos.roguelite.vulkan.uniform.UniformBufferBinding;

import static org.lwjgl.vulkan.VK10.*;

public class CameraUniformBufferObject extends DescriptorObject {
    public static final DescriptorBinding CAMERA_DESCRIPTOR_BINDING = new DescriptorBinding(0,
                                                                                            VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                                                            1,
                                                                                            VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
    public static final DescriptorBinding INSTANCE_DESCRIPTOR_BINDING = new DescriptorBinding(1,
                                                                                              VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                                                              1,
                                                                                              VK_SHADER_STAGE_VERTEX_BIT);

    // FIXME: Instance binding does not belong here, move to per-entity transform presentable state
    //        once something like that is implemented
    private final InstanceMatrices instanceMatrices;
    private final CameraMatrices cameraMatrices;

    private final Vector3f lookAtTarget;
    private final Vector3f up;

    public CameraUniformBufferObject(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout
    ) {
        this(deviceContext,
             swapchain,
             descriptorPool,
             layout,
             new InstanceMatrices(),
             new CameraMatrices());
    }

    private CameraUniformBufferObject(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout,
            final InstanceMatrices instanceMatrices,
            final CameraMatrices cameraMatrices
    ) {
        super(deviceContext,
              swapchain,
              descriptorPool,
              layout,
              new CombinedImageSamplerBinding[0],
              new UniformBufferBinding[]{cameraMatrices, instanceMatrices});

        this.cameraMatrices = cameraMatrices;
        this.instanceMatrices = instanceMatrices;

        final var lookAtDistance = 7.5f;
        this.cameraMatrices.eyePosition.set(0.0f, -1.0f, 1.5f)
                                       .normalize()
                                       .mul(lookAtDistance);
        this.lookAtTarget = new Vector3f(0.0f, 0.0f, 0.0f);
        this.up = new Vector3f(0.0f, 0.0f, 1.0f);
    }

    public void update(final int imageIndex, final double angle) {
        final var swapchainExtent = getSwapchain().getExtent();
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

        this.cameraMatrices.view.identity()
                                .lookAt(this.cameraMatrices.eyePosition, this.lookAtTarget, this.up)
                                .rotateZ((float) angle);

        this.instanceMatrices.model.identity();

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

    private static class InstanceMatrices implements UniformBufferBinding {
        private static final int OFFSET_MODEL = 0;

        private final Matrix4f model = new Matrix4f().identity();

        @Override
        public int binding() {
            return 1;
        }

        @Override
        public long sizeInBytes() {
            return 16 * Float.BYTES;
        }

        @Override
        public void write(final int offset, final ByteBuffer buffer) {
            this.model.get(offset + OFFSET_MODEL, buffer);
        }
    }
}
