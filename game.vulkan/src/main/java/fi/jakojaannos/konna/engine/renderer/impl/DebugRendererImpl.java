package fi.jakojaannos.konna.engine.renderer.impl;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.file.Path;

import fi.jakojaannos.konna.engine.CameraUniformBufferObject;
import fi.jakojaannos.konna.engine.PresentableState;
import fi.jakojaannos.konna.engine.renderer.DebugRenderer;
import fi.jakojaannos.konna.engine.renderer.debug.DebugLineVertex;
import fi.jakojaannos.konna.engine.util.RecreateCloseable;
import fi.jakojaannos.konna.engine.vulkan.GPUMesh;
import fi.jakojaannos.konna.engine.vulkan.command.CommandBuffer;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.engine.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.konna.engine.vulkan.rendering.RenderPass;
import fi.jakojaannos.konna.engine.vulkan.rendering.Swapchain;
import fi.jakojaannos.konna.engine.vulkan.types.VkPrimitiveTopology;
import fi.jakojaannos.roguelite.engine.data.components.Transform;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class DebugRendererImpl extends RecreateCloseable implements DebugRenderer {
    private final GPUMesh<DebugLineVertex> transformMesh;
    private final GraphicsPipeline<DebugLineVertex> linePipeline;

    // TODO: Separate recorder and renderer
    private PresentableState writeState;
    private PresentableState readState;

    public void setWriteState(final PresentableState state) {
        this.writeState = state;
    }

    public void setReadState(final PresentableState state) {
        this.readState = state;
    }

    public DebugRendererImpl(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final RenderPass renderPass,
            final Path assetRoot,
            final DescriptorSetLayout cameraDescriptorLayout
    ) {
        final var transformVertices = new DebugLineVertex[]{
                new DebugLineVertex(new Vector3f(0, 0, 0), new Vector3f(1.0f, 0.0f, 0.0f)),
                new DebugLineVertex(new Vector3f(1, 0, 0), new Vector3f(1.0f, 0.0f, 0.0f)),

                new DebugLineVertex(new Vector3f(0, 0, 0), new Vector3f(0.0f, 1.0f, 0.0f)),
                new DebugLineVertex(new Vector3f(0, 1, 0), new Vector3f(0.0f, 1.0f, 0.0f)),

                new DebugLineVertex(new Vector3f(0, 0, 0), new Vector3f(0.0f, 0.0f, 1.0f)),
                new DebugLineVertex(new Vector3f(0, 0, 1), new Vector3f(0.0f, 0.0f, 1.0f)),
        };
        final var transformIndices = new Integer[]{0, 1, 2, 3, 4, 5};

        this.transformMesh = new GPUMesh<>(deviceContext,
                                           DebugLineVertex.FORMAT,
                                           transformVertices,
                                           transformIndices);

        this.linePipeline = new GraphicsPipeline<>(deviceContext,
                                                   swapchain,
                                                   renderPass,
                                                   assetRoot.resolve("shaders/vulkan/debug/line.vert"),
                                                   assetRoot.resolve("shaders/vulkan/debug/line.frag"),
                                                   VkPrimitiveTopology.LINE_LIST,
                                                   DebugLineVertex.FORMAT,
                                                   cameraDescriptorLayout);
    }

    @Override
    public void drawTransform(final Transform transform) {
        final var entry = this.writeState.transforms().get();
        entry.position.set(transform.position.x,
                           transform.position.y,
                           0.0d);
        entry.rotation = (float) transform.rotation;
    }

    public void flush(
            final CameraUniformBufferObject cameraUBO,
            final CommandBuffer commandBuffer,
            final int imageIndex
    ) {
        try (final var stack = stackPush()) {
            final var pushConstantData = stack.malloc(16 * Float.BYTES);
            final var modelMatrix = new Matrix4f();

            vkCmdBindPipeline(commandBuffer.getHandle(),
                              VK_PIPELINE_BIND_POINT_GRAPHICS,
                              this.linePipeline.getHandle());

            vkCmdBindDescriptorSets(commandBuffer.getHandle(),
                                    VK_PIPELINE_BIND_POINT_GRAPHICS,
                                    this.linePipeline.getLayout(),
                                    0,
                                    stack.longs(cameraUBO.getDescriptorSet(imageIndex)),
                                    null);

            for (final var transform : this.readState.transforms()) {
                modelMatrix.identity()
                           .translate(transform.position)
                           .rotateZ(transform.rotation);
                modelMatrix.get(0, pushConstantData);

                vkCmdPushConstants(commandBuffer.getHandle(),
                                   this.linePipeline.getLayout(),
                                   VK_SHADER_STAGE_VERTEX_BIT,
                                   0,
                                   pushConstantData);

                this.transformMesh.draw(commandBuffer);
            }
        }
    }

    @Override
    protected void recreate() {
        this.linePipeline.tryRecreate();
    }

    @Override
    protected void cleanup() {
    }

    @Override
    public void close() {
        super.close();
        this.linePipeline.close();
        this.transformMesh.close();
    }
}
