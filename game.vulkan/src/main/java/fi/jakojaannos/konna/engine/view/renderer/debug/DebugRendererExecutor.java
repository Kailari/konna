package fi.jakojaannos.konna.engine.view.renderer.debug;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import fi.jakojaannos.konna.engine.CameraDescriptor;
import fi.jakojaannos.konna.engine.application.PresentableState;
import fi.jakojaannos.konna.engine.assets.AssetManager;
import fi.jakojaannos.konna.engine.assets.Mesh;
import fi.jakojaannos.konna.engine.util.RecreateCloseable;
import fi.jakojaannos.konna.engine.vulkan.RenderingBackend;
import fi.jakojaannos.konna.engine.vulkan.command.CommandBuffer;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.konna.engine.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.konna.engine.vulkan.rendering.RenderPass;
import fi.jakojaannos.konna.engine.vulkan.types.VkPrimitiveTopology;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class DebugRendererExecutor extends RecreateCloseable {
    private final Mesh transformMesh;
    private final GraphicsPipeline<DebugLineVertex> linePipeline;

    public DebugRendererExecutor(
            final RenderingBackend backend,
            final RenderPass renderPass,
            final AssetManager assetManager,
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


        this.transformMesh = Mesh.from(backend,
                                       DebugLineVertex.FORMAT,
                                       transformVertices);

        this.linePipeline = new GraphicsPipeline<>(backend.deviceContext(),
                                                   backend.swapchain(),
                                                   renderPass,
                                                   assetManager,
                                                   "shaders/vulkan/debug/line.vert",
                                                   "shaders/vulkan/debug/line.frag",
                                                   VkPrimitiveTopology.LINE_LIST,
                                                   DebugLineVertex.FORMAT,
                                                   cameraDescriptorLayout);
    }

    public void flush(
            final PresentableState state,
            final CameraDescriptor cameraUBO,
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

            vkCmdBindVertexBuffers(commandBuffer.getHandle(),
                                   0,
                                   stack.longs(this.transformMesh.getVertexBuffer().getHandle()),
                                   stack.longs(0L));

            for (final var transform : state.transforms()) {
                modelMatrix.identity()
                           .translate(transform.position)
                           .rotateZ(transform.rotation);
                modelMatrix.get(0, pushConstantData);

                vkCmdPushConstants(commandBuffer.getHandle(),
                                   this.linePipeline.getLayout(),
                                   VK_SHADER_STAGE_VERTEX_BIT,
                                   0,
                                   pushConstantData);

                vkCmdDraw(commandBuffer.getHandle(),
                          6,
                          1,
                          0,
                          0);
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
