package fi.jakojaannos.roguelite.vulkan;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.CameraUniformBufferObject;
import fi.jakojaannos.roguelite.MaterialInstance;
import fi.jakojaannos.roguelite.SceneUniformBufferObject;
import fi.jakojaannos.roguelite.assets.Mesh;
import fi.jakojaannos.roguelite.assets.MeshLoader;
import fi.jakojaannos.roguelite.assets.MeshVertex;
import fi.jakojaannos.roguelite.vulkan.command.CommandBuffer;
import fi.jakojaannos.roguelite.vulkan.command.CommandPool;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.roguelite.vulkan.rendering.Framebuffers;
import fi.jakojaannos.roguelite.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.roguelite.vulkan.rendering.RenderPass;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;
import fi.jakojaannos.roguelite.vulkan.window.Window;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class Renderer implements AutoCloseable {
    private final Swapchain swapchain;
    private final DepthTexture depthTexture;
    private final RenderPass renderPass;
    private final Framebuffers framebuffers;
    private final CommandPool commandPool;
    private final DescriptorPool descriptorPool;

    private final GraphicsPipeline<MeshVertex> graphicsPipeline;
    private final DescriptorSetLayout materialDescriptorLayout;
    private final CameraUniformBufferObject cameraUBO;
    private final SceneUniformBufferObject sceneUBO;
    private final TextureSampler textureSampler;

    private final MeshLoader meshLoader;
    private final Mesh[] meshes;

    private CommandBuffer[] commandBuffers;

    public int getSwapchainImageCount() {
        return this.swapchain.getImageCount();
    }

    public Swapchain getSwapchain() {
        return this.swapchain;
    }

    public CameraUniformBufferObject getCameraUBO() {
        return this.cameraUBO;
    }

    public Renderer(final Path assetRoot, final RenderingBackend backend, final Window window) {
        this.commandPool = backend.deviceContext().getGraphicsCommandPool();
        this.swapchain = new Swapchain(backend.deviceContext(), window, backend.surface());

        this.depthTexture = new DepthTexture(backend.deviceContext(), this.swapchain);

        this.renderPass = new RenderPass(backend.deviceContext(), this.swapchain);
        this.framebuffers = new Framebuffers(backend.deviceContext(),
                                             this.swapchain,
                                             this.depthTexture,
                                             this.renderPass);

        // We have swapchainImageCount copies of two descriptor sets. Why use suppliers? That way we
        // can delay the descriptorCount/maxSets calculations to `tryRecreate`, where all resources
        // are already initialized. E.g. we do not yet know the image count here
        this.descriptorPool = new DescriptorPool(backend.deviceContext(),
                                                 () -> this.swapchain.getImageCount() * 3 + this.swapchain.getImageCount() * 7,
                                                 new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                                         () -> this.swapchain.getImageCount() * 2 + this.swapchain
                                                                                                                            .getImageCount() * 7),
                                                 new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                                                                         this.swapchain::getImageCount));
        this.cameraUBO = new CameraUniformBufferObject(backend.deviceContext(),
                                                       this.swapchain,
                                                       this.descriptorPool);
        this.sceneUBO = new SceneUniformBufferObject(backend.deviceContext(),
                                                     this.swapchain,
                                                     this.descriptorPool);

        this.textureSampler = new TextureSampler(backend.deviceContext());

        this.materialDescriptorLayout = new DescriptorSetLayout(backend.deviceContext(),
                                                                MaterialInstance.TEXTURE_DESCRIPTOR_BINDING,
                                                                MaterialInstance.MATERIAL_DESCRIPTOR_BINDING);

        this.graphicsPipeline = new GraphicsPipeline<>(assetRoot,
                                                       backend.deviceContext(),
                                                       this.swapchain,
                                                       this.renderPass,
                                                       MeshVertex.FORMAT,
                                                       this.cameraUBO.getLayout(),
                                                       this.sceneUBO.getLayout(),
                                                       this.materialDescriptorLayout);

        this.meshLoader = new MeshLoader(backend.deviceContext(),
                                         this.swapchain,
                                         this.descriptorPool,
                                         this.materialDescriptorLayout,
                                         this.textureSampler,
                                         assetRoot);
        this.meshes = this.meshLoader.load(Path.of("models/arena.obj"));

        /*
        this.mesh = new GPUMesh<>(backend.deviceContext(),
                                  Vertex.FORMAT,
                                  new Vertex[]{
                                       new Vertex(new Vector3f(-0.5f, -0.5f, 0.0f), new Vector2f(0.0f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f)),
                                       new Vertex(new Vector3f(0.5f, -0.5f, 0.0f), new Vector2f(1.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f)),
                                       new Vertex(new Vector3f(0.5f, 0.5f, 0.0f), new Vector2f(1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 1.0f)),
                                       new Vertex(new Vector3f(-0.5f, 0.5f, 0.0f), new Vector2f(0.0f, 1.0f), new Vector3f(1.0f, 0.0f, 1.0f)),

                                       new Vertex(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector2f(0.0f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f)),
                                       new Vertex(new Vector3f(0.5f, -0.5f, -0.5f), new Vector2f(1.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f)),
                                       new Vertex(new Vector3f(0.5f, 0.5f, -0.5f), new Vector2f(1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 1.0f)),
                                       new Vertex(new Vector3f(-0.5f, 0.5f, -0.5f), new Vector2f(0.0f, 1.0f), new Vector3f(1.0f, 0.0f, 1.0f)),
                               },
                                  new Short[]{
                                       0, 1, 2,
                                       2, 3, 0,

                                       4, 5, 6,
                                       6, 7, 4,
                               });*/

        recreateSwapchain();
    }

    public CommandBuffer getCommands(final int imageIndex) {
        return this.commandBuffers[imageIndex];
    }

    public void recreateSwapchain() {
        freeCommandBuffers();

        this.swapchain.tryRecreate();
        this.depthTexture.tryRecreate();

        this.renderPass.tryRecreate();
        this.framebuffers.tryRecreate();

        this.descriptorPool.tryRecreate();
        this.cameraUBO.tryRecreate();
        this.sceneUBO.tryRecreate();
        // Recreate mesh material instances
        for (final var mesh : this.meshes) {
            mesh.tryRecreate();
        }
        this.graphicsPipeline.tryRecreate();

        recordCommandBuffers();
    }

    private void recordCommandBuffers() {
        this.commandBuffers = this.commandPool.allocate(this.swapchain.getImageCount());
        for (int imageIndex = 0; imageIndex < this.commandBuffers.length; imageIndex++) {
            final var commandBuffer = this.commandBuffers[imageIndex];
            final var framebuffer = this.framebuffers.get(imageIndex);

            try (final var stack = stackPush();
                 final var ignored = commandBuffer.begin();
                 final var ignored2 = this.renderPass.begin(framebuffer, commandBuffer)
            ) {
                vkCmdBindPipeline(commandBuffer.getHandle(),
                                  VK_PIPELINE_BIND_POINT_GRAPHICS,
                                  this.graphicsPipeline.getHandle());
                vkCmdBindDescriptorSets(commandBuffer.getHandle(),
                                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                                        this.graphicsPipeline.getLayout(),
                                        0,
                                        stack.longs(this.cameraUBO.getDescriptorSet(imageIndex),
                                                    this.sceneUBO.getDescriptorSet(imageIndex)),
                                        null);


                for (final var mesh : this.meshes) {
                    // FIXME: Get from mesh material
                    vkCmdBindDescriptorSets(commandBuffer.getHandle(),
                                            VK_PIPELINE_BIND_POINT_GRAPHICS,
                                            this.graphicsPipeline.getLayout(),
                                            2,
                                            stack.longs(mesh.getMaterialInstance().getDescriptorSet(imageIndex)),
                                            null);

                    vkCmdBindVertexBuffers(commandBuffer.getHandle(),
                                           0,
                                           stack.longs(mesh.getVertexBuffer().getHandle()),
                                           stack.longs(0L));
                    vkCmdBindIndexBuffer(commandBuffer.getHandle(),
                                         mesh.getIndexBuffer().getHandle(),
                                         0,
                                         VK_INDEX_TYPE_UINT16);

                    vkCmdDrawIndexed(commandBuffer.getHandle(),
                                     mesh.getIndexCount(),
                                     1,
                                     0,
                                     0,
                                     0);
                }
            }
        }
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
            vkFreeCommandBuffers(this.commandPool.getDevice(),
                                 this.commandPool.getHandle(),
                                 pBuffers);
        }
    }

    @Override
    public void close() {
        this.descriptorPool.close();
        this.cameraUBO.close();
        this.sceneUBO.close();
        this.materialDescriptorLayout.close();

        this.textureSampler.close();
        this.meshLoader.close();
        for (final Mesh mesh : this.meshes) {
            mesh.close();
        }

        this.depthTexture.close();

        this.framebuffers.close();
        this.renderPass.close();
        this.graphicsPipeline.close();

        this.swapchain.close();
    }
}
