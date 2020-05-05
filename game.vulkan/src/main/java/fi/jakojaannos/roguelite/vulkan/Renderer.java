package fi.jakojaannos.roguelite.vulkan;

import org.joml.Vector3f;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.CameraUBO;
import fi.jakojaannos.roguelite.vulkan.command.CommandBuffer;
import fi.jakojaannos.roguelite.vulkan.command.CommandPool;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.Framebuffers;
import fi.jakojaannos.roguelite.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.roguelite.vulkan.rendering.RenderPass;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;
import fi.jakojaannos.roguelite.vulkan.uniform.DescriptorPool;
import fi.jakojaannos.roguelite.vulkan.window.Window;

import static fi.jakojaannos.roguelite.util.VkUtil.ensureSuccess;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class Renderer implements AutoCloseable {
    private final DeviceContext deviceContext;

    private final Swapchain swapchain;
    private final RenderPass renderPass;
    private final Framebuffers framebuffers;
    private final CommandPool commandPool;
    private DescriptorPool descriptorPool;

    private final GraphicsPipeline<Vertex> graphicsPipeline;
    private final Mesh<Vertex> mesh;
    private final CameraUBO ubo;

    private CommandBuffer[] commandBuffers;
    private long[] descriptorSets;

    public int getSwapchainImageCount() {
        return this.swapchain.getImageCount();
    }

    public Swapchain getSwapchain() {
        return this.swapchain;
    }

    public CameraUBO getCameraUBO() {
        return this.ubo;
    }

    public Renderer(final Path assetRoot, final RenderingBackend backend, final Window window) {
        this.deviceContext = backend.deviceContext();

        this.swapchain = new Swapchain(backend.deviceContext(), window, backend.surface());

        this.renderPass = new RenderPass(backend.deviceContext(), this.swapchain);
        this.framebuffers = new Framebuffers(backend.deviceContext(),
                                             this.swapchain,
                                             this.renderPass);

        this.ubo = new CameraUBO(backend.deviceContext(), this.swapchain);
        this.graphicsPipeline = new GraphicsPipeline<>(assetRoot,
                                                       backend.deviceContext(),
                                                       this.swapchain,
                                                       this.renderPass,
                                                       Vertex.FORMAT,
                                                       this.ubo.getUBO());

        this.commandPool = new CommandPool(backend.deviceContext().getDevice(),
                                           backend.deviceContext()
                                                  .getQueueFamilies()
                                                  .graphics());
        this.descriptorPool = new DescriptorPool(backend.deviceContext(),
                                                 this.swapchain.getImageCount(),
                                                 new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                                         this.swapchain.getImageCount()));

        this.mesh = new Mesh<>(backend.deviceContext(),
                               Vertex.FORMAT,
                               new Vertex[]{
                                       new Vertex(new Vector3f(-0.5f, -0.5f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f)),
                                       new Vertex(new Vector3f(0.5f, -0.5f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f)),
                                       new Vertex(new Vector3f(0.5f, 0.5f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f)),
                                       new Vertex(new Vector3f(-0.5f, 0.5f, 0.0f), new Vector3f(1.0f, 0.0f, 1.0f)),
                               },
                               new Short[]{
                                       0, 1, 2,
                                       2, 3, 0
                               });
        // TODO: Call recreate here? Remove all tryRecreate calls from ctors and trigger a re-create here
        createDescriptorSets();
        recordCommandBuffers();
    }

    public CommandBuffer getCommands(final int imageIndex) {
        return this.commandBuffers[imageIndex];
    }

    public void recreateSwapchain() {
        this.descriptorPool.close();
        this.descriptorPool = new DescriptorPool(this.deviceContext,
                                                 this.swapchain.getImageCount(),
                                                 new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                                         this.swapchain.getImageCount()));
        freeCommandBuffers();

        this.swapchain.tryRecreate();

        this.renderPass.tryRecreate();
        this.ubo.tryRecreate();
        this.graphicsPipeline.tryRecreate();
        this.framebuffers.tryRecreate();

        createDescriptorSets();
        recordCommandBuffers();
    }

    // FIXME: Could this be part of the UBO itself? Or the GraphicsPipeline?
    private void createDescriptorSets() {
        this.descriptorSets = new long[this.swapchain.getImageCount()];

        try (final var stack = stackPush()) {
            final var layouts = stack.mallocLong(this.swapchain.getImageCount());
            for (int i = 0; i < this.swapchain.getImageCount(); i++) {
                layouts.put(i, this.ubo.getUBO().getLayoutHandle());
            }
            final var allocateInfo = VkDescriptorSetAllocateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                    .descriptorPool(this.descriptorPool.getHandle())
                    .pSetLayouts(layouts);

            ensureSuccess(vkAllocateDescriptorSets(this.deviceContext.getDevice(),
                                                   allocateInfo,
                                                   this.descriptorSets),
                          "Allocating descriptor sets failed!");

            for (int i = 0; i < this.descriptorSets.length; i++) {
                final var buffer = this.ubo.getUBO().getBindingBuffer(0, i);
                final var bufferInfo = VkDescriptorBufferInfo
                        .callocStack(1)
                        .buffer(buffer.getHandle())
                        .offset(0)
                        .range(buffer.getSize());

                final var descriptorWrites = VkWriteDescriptorSet
                        .callocStack(1)
                        .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                        .dstSet(this.descriptorSets[i])
                        // FIXME: These values exist in the binding! Should this happen there?
                        .dstBinding(0)
                        .dstArrayElement(0)
                        .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                        .descriptorCount(1)
                        // -------------------------------------------------------------------
                        .pBufferInfo(bufferInfo);

                // NOTE: Why don't we need to call this every frame? Well, the bindings allocate the buffers
                //       as HOST_COHERENT, so we point the GPU at the memory once and it knows where to look
                //       from there on.
                // FIXME: Move this to binding update() and use property flags to determine if this is necessary
                vkUpdateDescriptorSets(this.deviceContext.getDevice(), descriptorWrites, null);
            }
        }
    }

    private void recordCommandBuffers() {
        this.commandBuffers = this.commandPool.allocate(this.swapchain.getImageCount());
        for (int i = 0; i < this.commandBuffers.length; i++) {
            final var commandBuffer = this.commandBuffers[i];
            final var descriptorSet = this.descriptorSets[i];
            final var framebuffer = this.framebuffers.get(i);

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
                                        stack.longs(descriptorSet),
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
        this.ubo.close();
        this.mesh.close();

        this.commandPool.close();
        this.framebuffers.close();
        this.renderPass.close();
        this.graphicsPipeline.close();

        this.swapchain.close();
    }
}
