package fi.jakojaannos.roguelite.vulkan;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.CameraUniformBufferObject;
import fi.jakojaannos.roguelite.TextureDescriptor;
import fi.jakojaannos.roguelite.vulkan.command.CommandBuffer;
import fi.jakojaannos.roguelite.vulkan.command.CommandPool;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.roguelite.vulkan.rendering.Framebuffers;
import fi.jakojaannos.roguelite.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.roguelite.vulkan.rendering.RenderPass;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;
import fi.jakojaannos.roguelite.vulkan.textures.GPUImage;
import fi.jakojaannos.roguelite.vulkan.window.Window;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class Renderer implements AutoCloseable {
    private final Swapchain swapchain;
    private final RenderPass renderPass;
    private final Framebuffers framebuffers;
    private final CommandPool commandPool;
    private final DescriptorPool descriptorPool;

    private final GraphicsPipeline<Vertex> graphicsPipeline;
    private final CameraUniformBufferObject cameraUBO;
    private final Mesh<Vertex> mesh;
    private final GPUImage image;
    private final Texture texture;
    private final TextureDescriptor textureDescriptor;

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
        this.renderPass = new RenderPass(backend.deviceContext(), this.swapchain);
        this.framebuffers = new Framebuffers(backend.deviceContext(),
                                             this.swapchain,
                                             this.renderPass);

        // We have swapchainImageCount copies of two descriptor sets
        this.descriptorPool = new DescriptorPool(backend.deviceContext(),
                                                 this.swapchain.getImageCount() * 2,
                                                 new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                                         this.swapchain.getImageCount()),
                                                 new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                                                                         this.swapchain.getImageCount()));
        this.cameraUBO = new CameraUniformBufferObject(backend.deviceContext(),
                                                       this.swapchain,
                                                       this.descriptorPool);
        this.image = new GPUImage(backend.deviceContext(),
                                  assetRoot.resolve("textures/vulkan/texture.jpg"),
                                  VK_IMAGE_TILING_OPTIMAL,
                                  VK_IMAGE_USAGE_SAMPLED_BIT,
                                  VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        this.texture = new Texture(backend.deviceContext(), this.image);
        this.textureDescriptor = new TextureDescriptor(backend.deviceContext(),
                                                       this.swapchain,
                                                       this.descriptorPool,
                                                       this.texture);
        this.graphicsPipeline = new GraphicsPipeline<>(assetRoot,
                                                       backend.deviceContext(),
                                                       this.swapchain,
                                                       this.renderPass,
                                                       Vertex.FORMAT,
                                                       this.cameraUBO.getLayout(),
                                                       this.textureDescriptor.getLayout());

        this.mesh = new Mesh<>(backend.deviceContext(),
                               Vertex.FORMAT,
                               new Vertex[]{
                                       new Vertex(new Vector3f(-0.5f, -0.5f, 0.0f), new Vector2f(0.0f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f)),
                                       new Vertex(new Vector3f(0.5f, -0.5f, 0.0f), new Vector2f(1.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f)),
                                       new Vertex(new Vector3f(0.5f, 0.5f, 0.0f), new Vector2f(1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 1.0f)),
                                       new Vertex(new Vector3f(-0.5f, 0.5f, 0.0f), new Vector2f(0.0f, 1.0f), new Vector3f(1.0f, 0.0f, 1.0f)),
                               },
                               new Short[]{
                                       0, 1, 2,
                                       2, 3, 0
                               });

        // TODO: Call recreate here? Remove all tryRecreate calls from ctors and trigger a re-create here
        recordCommandBuffers();
    }

    public CommandBuffer getCommands(final int imageIndex) {
        return this.commandBuffers[imageIndex];
    }

    public void recreateSwapchain() {
        freeCommandBuffers();

        this.swapchain.tryRecreate();
        this.renderPass.tryRecreate();
        this.framebuffers.tryRecreate();

        this.descriptorPool.tryRecreate();
        this.cameraUBO.tryRecreate();
        this.textureDescriptor.tryRecreate();
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
                                                    this.textureDescriptor.get(imageIndex)),
                                        null);

                vkCmdBindVertexBuffers(commandBuffer.getHandle(),
                                       0,
                                       stack.longs(this.mesh.getVertexBuffer().getHandle()),
                                       stack.longs(0L));
                vkCmdBindIndexBuffer(commandBuffer.getHandle(),
                                     this.mesh.getIndexBuffer().getHandle(),
                                     0,
                                     VK_INDEX_TYPE_UINT16);

                vkCmdDrawIndexed(commandBuffer.getHandle(),
                                 this.mesh.getIndexCount(),
                                 1,
                                 0,
                                 0,
                                 0);
            }
        }
    }

    private void freeCommandBuffers() {
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
        this.textureDescriptor.close();

        this.texture.close();
        this.image.close();
        this.mesh.close();

        this.framebuffers.close();
        this.renderPass.close();
        this.graphicsPipeline.close();

        this.swapchain.close();
    }
}
