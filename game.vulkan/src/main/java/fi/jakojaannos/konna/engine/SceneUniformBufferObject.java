package fi.jakojaannos.konna.engine;

import org.joml.Vector3f;

import java.nio.ByteBuffer;

import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorBinding;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.engine.vulkan.rendering.Swapchain;
import fi.jakojaannos.konna.engine.vulkan.descriptor.CombinedImageSamplerBinding;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorObject;
import fi.jakojaannos.konna.engine.vulkan.descriptor.UniformBufferBinding;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

public class SceneUniformBufferObject extends DescriptorObject {
    public static final DescriptorBinding LIGHT_COUNT_DESCRIPTOR_BINDING = new DescriptorBinding(
            1,
            VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
            1,
            VK_SHADER_STAGE_FRAGMENT_BIT);
    public static final DescriptorBinding LIGHTS_DESCRIPTOR_BINDING = new DescriptorBinding(
            0,
            VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
            1,
            VK_SHADER_STAGE_FRAGMENT_BIT);

    private static final int MAX_LIGHTS = 10;

    public SceneUniformBufferObject(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout
    ) {
        this(deviceContext,
             swapchain,
             descriptorPool,
             layout,
             new LightsBinding(),
             new LightCountBinding());
    }

    private SceneUniformBufferObject(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout,
            final LightsBinding lights,
            final LightCountBinding lightCount
    ) {
        super(deviceContext,
              swapchain,
              descriptorPool,
              layout,
              new CombinedImageSamplerBinding[0],
              new UniformBufferBinding[]{
                      lights,
                      lightCount,
              });

        lightCount.count = 1;
        lights.colors[0] = new Vector3f(1.0f, 1.0f, 1.0f);
        lights.positions[0] = new Vector3f(0.0f, 0.0f, 20.0f);
        lights.radius[0] = 60.0f;
    }

    private static class LightsBinding implements UniformBufferBinding {
        private static final int OFFSET_POSITION = 0;
        private static final int OFFSET_RADIUS = 3 * Float.BYTES;
        private static final int OFFSET_COLOR = 4 * Float.BYTES;

        private static final int STRIDE = 8 * Float.BYTES;

        private final Vector3f[] positions = new Vector3f[MAX_LIGHTS];
        private final Vector3f[] colors = new Vector3f[MAX_LIGHTS];
        private final float[] radius = new float[MAX_LIGHTS];

        @Override
        public int binding() {
            return 0;
        }

        @Override
        public long sizeInBytes() {
            return MAX_LIGHTS * STRIDE;
        }

        @Override
        public void write(final int offset, final ByteBuffer buffer) {
            for (int i = 0; i < MAX_LIGHTS; ++i) {
                if (this.positions[i] == null) {
                    continue;
                }

                final var actualOffset = offset + STRIDE * i;

                this.positions[i].get(actualOffset + OFFSET_POSITION, buffer);
                this.colors[i].get(actualOffset + OFFSET_COLOR, buffer);
                buffer.putFloat(actualOffset + OFFSET_RADIUS, this.radius[i]);
            }
        }
    }

    private static class LightCountBinding implements UniformBufferBinding {
        private static final int OFFSET_COUNT = 0;

        private int count;

        @Override
        public int binding() {
            return 1;
        }

        @Override
        public long sizeInBytes() {
            return Integer.BYTES;
        }

        @Override
        public void write(final int offset, final ByteBuffer buffer) {
            buffer.putInt(offset + OFFSET_COUNT, this.count);
        }
    }
}
