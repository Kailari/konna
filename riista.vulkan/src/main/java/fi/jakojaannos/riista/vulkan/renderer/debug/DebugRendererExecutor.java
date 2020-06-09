package fi.jakojaannos.riista.vulkan.renderer.debug;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import fi.jakojaannos.riista.vulkan.util.RecreateCloseable;
import fi.jakojaannos.riista.vulkan.internal.RenderingBackend;
import fi.jakojaannos.riista.vulkan.application.PresentableState;
import fi.jakojaannos.riista.vulkan.internal.command.CommandBuffer;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorSetLayout;
import fi.jakojaannos.riista.vulkan.internal.types.VkPrimitiveTopology;
import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.vulkan.CameraDescriptor;
import fi.jakojaannos.riista.vulkan.assets.mesh.MeshImpl;
import fi.jakojaannos.riista.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.riista.vulkan.rendering.RenderPass;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class DebugRendererExecutor extends RecreateCloseable {
    private final MeshImpl transformMesh;
    private final MeshImpl cubeMesh;
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
        final var cubeVertices = new DebugLineVertex[]{
                new DebugLineVertex(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)),
                new DebugLineVertex(new Vector3f(1, 0, 0), new Vector3f(1, 1, 1)),
                new DebugLineVertex(new Vector3f(1, 1, 0), new Vector3f(1, 1, 1)),
                new DebugLineVertex(new Vector3f(0, 1, 0), new Vector3f(1, 1, 1)),

                new DebugLineVertex(new Vector3f(0, 0, 1), new Vector3f(1, 1, 1)),
                new DebugLineVertex(new Vector3f(1, 0, 1), new Vector3f(1, 1, 1)),
                new DebugLineVertex(new Vector3f(1, 1, 1), new Vector3f(1, 1, 1)),
                new DebugLineVertex(new Vector3f(0, 1, 1), new Vector3f(1, 1, 1)),
        };
        final var cubeIndices = new Integer[]{
                0, 1, 1, 2, 2, 3, 3, 0,
                4, 5, 5, 6, 6, 7, 7, 4,
                0, 4, 1, 5, 2, 6, 3, 7
        };


        this.transformMesh = new MeshImpl(backend, DebugLineVertex.FORMAT, transformVertices, null, null);
        this.cubeMesh = new MeshImpl(backend, DebugLineVertex.FORMAT, cubeVertices, cubeIndices, null);

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

            vkCmdBindVertexBuffers(commandBuffer.getHandle(),
                                   0,
                                   stack.longs(this.cubeMesh.getVertexBuffer().getHandle()),
                                   stack.longs(0L));
            vkCmdBindIndexBuffer(commandBuffer.getHandle(),
                                 this.cubeMesh.getIndexBuffer().getHandle(),
                                 0,
                                 VK_INDEX_TYPE_UINT32);
            for (final var box : state.boxes()) {
                modelMatrix.identity()
                           .translate(box.position)
                           .rotateZ(box.rotation)
                           .translate(-box.offset.x, -box.offset.y, 0)
                           .scale(box.size.x, box.size.y, 1.0f);
                modelMatrix.get(0, pushConstantData);

                vkCmdPushConstants(commandBuffer.getHandle(),
                                   this.linePipeline.getLayout(),
                                   VK_SHADER_STAGE_VERTEX_BIT,
                                   0,
                                   pushConstantData);

                vkCmdDrawIndexed(commandBuffer.getHandle(),
                                 this.cubeMesh.getIndexCount(),
                                 1,
                                 0,
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
        this.transformMesh.close();
        this.cubeMesh.close();
        this.linePipeline.close();
    }
}
