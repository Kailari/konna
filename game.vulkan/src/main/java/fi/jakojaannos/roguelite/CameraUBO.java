package fi.jakojaannos.roguelite;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;
import fi.jakojaannos.roguelite.vulkan.uniform.DescriptorPool;
import fi.jakojaannos.roguelite.vulkan.uniform.UniformBinding;
import fi.jakojaannos.roguelite.vulkan.uniform.UniformBufferObject;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

public class CameraUBO implements AutoCloseable {
    private final Swapchain swapchain;
    private final UniformBufferObject ubo;
    private final UniformBinding<CameraMatrices> cameraBinding;
    private final UniformBinding<InstanceMatrices> instanceBinding;

    private final CameraMatrices cameraMatrices;
    private final InstanceMatrices instanceMatrices;

    private final Vector3f eyePosition;
    private final Vector3f lookAtTarget;
    private final Vector3f up;

    public UniformBufferObject getUBO() {
        return this.ubo;
    }

    public CameraUBO(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool
    ) {
        this.swapchain = swapchain;
        this.cameraBinding = new UniformBinding<>(deviceContext,
                                                  swapchain,
                                                  0,
                                                  VK_SHADER_STAGE_VERTEX_BIT,
                                                  1,
                                                  CameraMatrices.SIZE_IN_BYTES,
                                                  CameraMatrices::write);
        this.instanceBinding = new UniformBinding<>(deviceContext,
                                                    swapchain,
                                                    1,
                                                    VK_SHADER_STAGE_VERTEX_BIT,
                                                    1,
                                                    InstanceMatrices.SIZE_IN_BYTES,
                                                    InstanceMatrices::write);
        this.ubo = new UniformBufferObject(deviceContext,
                                           swapchain,
                                           descriptorPool,
                                           this.cameraBinding,
                                           this.instanceBinding);

        this.cameraMatrices = new CameraMatrices();
        this.instanceMatrices = new InstanceMatrices();

        this.eyePosition = new Vector3f(2.0f, 2.0f, 2.0f);
        this.lookAtTarget = new Vector3f(0.0f, 0.0f, 0.0f);
        this.up = new Vector3f(0.0f, 0.0f, 1.0f);
    }

    public void tryRecreate() {
        this.ubo.tryRecreate();
    }

    public void update(final int imageIndex, final double angle) {
        final var aspectRatio = this.swapchain.getExtent().width() / (float) this.swapchain.getExtent().height();
        this.cameraMatrices.projection.identity()
                                      .perspective((float) Math.toRadians(45.0),
                                                   aspectRatio,
                                                   0.1f, 1000.0f, true);
        // Flip the Y-axis
        this.cameraMatrices.projection.m11(this.cameraMatrices.projection.m11() * -1.0f);

        this.cameraMatrices.view.identity()
                                .lookAt(this.eyePosition, this.lookAtTarget, this.up);


        this.cameraBinding.update(imageIndex, 0, this.cameraMatrices);


        this.instanceMatrices.model.identity()
                                   .rotateZ((float) angle);

        this.instanceBinding.update(imageIndex, 0, this.instanceMatrices);
    }

    @Override
    public void close() {
        this.ubo.close();
    }

    private static class InstanceMatrices {
        private static final int OFFSET_MODEL = 0;

        private static final int SIZE_IN_BYTES = 16 * Float.BYTES;

        private final Matrix4f model = new Matrix4f().identity();

        public void write(final int offset, final ByteBuffer buffer) {
            this.model.get(offset + OFFSET_MODEL, buffer);
        }
    }

    private static class CameraMatrices {
        private static final int SIZE_IN_BYTES = 2 * 16 * Float.BYTES;

        private static final int OFFSET_VIEW = 0;
        private static final int OFFSET_PROJECTION = 16 * Float.BYTES;

        private final Matrix4f view = new Matrix4f().identity();
        private final Matrix4f projection = new Matrix4f().identity();

        public void write(final int offset, final ByteBuffer buffer) {
            this.view.get(offset + OFFSET_VIEW, buffer);
            this.projection.get(offset + OFFSET_PROJECTION, buffer);
        }
    }
}
