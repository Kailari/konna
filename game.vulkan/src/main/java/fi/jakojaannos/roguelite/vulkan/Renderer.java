package fi.jakojaannos.roguelite.vulkan;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.CameraUniformBufferObject;
import fi.jakojaannos.roguelite.MaterialInstance;
import fi.jakojaannos.roguelite.SceneUniformBufferObject;
import fi.jakojaannos.roguelite.SwapchainImageDependentDescriptorPool;
import fi.jakojaannos.roguelite.assets.*;
import fi.jakojaannos.roguelite.assets.loader.SkeletalMeshLoader;
import fi.jakojaannos.roguelite.assets.loader.StaticMeshLoader;
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

    private final GraphicsPipeline<StaticMeshVertex> staticPipeline;
    private final GraphicsPipeline<SkeletalMeshVertex> skeletalPipeline;
    private final DescriptorSetLayout materialDescriptorLayout;
    private final DescriptorSetLayout boneDescriptorLayout;
    private final DescriptorSetLayout cameraDescriptorLayout;
    private final DescriptorSetLayout sceneDescriptorLayout;
    private final CameraUniformBufferObject cameraUBO;
    private final SceneUniformBufferObject sceneUBO;
    private final TextureSampler textureSampler;
    private final Texture defaultTexture;

    private final StaticMesh[] staticMeshes;

    private final AnimatedMesh humanoid;

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

    public AnimatedMesh getHumanoid() {
        return this.humanoid;
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
        this.descriptorPool = new SwapchainImageDependentDescriptorPool(
                backend.deviceContext(),
                this.swapchain,
                2 + 7 + 1,
                new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                        () -> this.swapchain.getImageCount() * (2 + 7)),
                new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                                        this.swapchain::getImageCount));
        this.cameraDescriptorLayout = new DescriptorSetLayout(backend.deviceContext(),
                                                              CameraUniformBufferObject.CAMERA_DESCRIPTOR_BINDING,
                                                              CameraUniformBufferObject.INSTANCE_DESCRIPTOR_BINDING);
        this.cameraUBO = new CameraUniformBufferObject(backend.deviceContext(),
                                                       this.swapchain,
                                                       this.descriptorPool,
                                                       this.cameraDescriptorLayout);

        this.sceneDescriptorLayout = new DescriptorSetLayout(backend.deviceContext(),
                                                             SceneUniformBufferObject.LIGHTS_DESCRIPTOR_BINDING,
                                                             SceneUniformBufferObject.LIGHT_COUNT_DESCRIPTOR_BINDING);
        this.sceneUBO = new SceneUniformBufferObject(backend.deviceContext(),
                                                     this.swapchain,
                                                     this.descriptorPool,
                                                     this.sceneDescriptorLayout);

        this.textureSampler = new TextureSampler(backend.deviceContext());

        this.materialDescriptorLayout = new DescriptorSetLayout(backend.deviceContext(),
                                                                MaterialInstance.TEXTURE_DESCRIPTOR_BINDING,
                                                                MaterialInstance.MATERIAL_DESCRIPTOR_BINDING);
        this.boneDescriptorLayout = new DescriptorSetLayout(backend.deviceContext(),
                                                            AnimatedMesh.BONE_DESCRIPTOR_BINDING);

        this.staticPipeline = new GraphicsPipeline<>(backend.deviceContext(),
                                                     this.swapchain,
                                                     this.renderPass,
                                                     assetRoot.resolve("shaders/vulkan/staticMesh.vert"),
                                                     assetRoot.resolve("shaders/vulkan/shader.frag"),
                                                     StaticMeshVertex.FORMAT,
                                                     this.cameraUBO.getLayout(),
                                                     this.sceneUBO.getLayout(),
                                                     this.materialDescriptorLayout);
        this.skeletalPipeline = new GraphicsPipeline<>(backend.deviceContext(),
                                                       this.swapchain,
                                                       this.renderPass,
                                                       assetRoot.resolve("shaders/vulkan/skeletalMesh.vert"),
                                                       assetRoot.resolve("shaders/vulkan/shader.frag"),
                                                       SkeletalMeshVertex.FORMAT,
                                                       this.cameraUBO.getLayout(),
                                                       this.sceneUBO.getLayout(),
                                                       this.materialDescriptorLayout,
                                                       this.boneDescriptorLayout);

        this.defaultTexture = new Texture(backend.deviceContext(),
                                          assetRoot.resolve("textures/vulkan/texture.jpg"));
        final StaticMeshLoader staticMeshLoader = new StaticMeshLoader(backend.deviceContext(),
                                                                       this.swapchain,
                                                                       this.descriptorPool,
                                                                       this.materialDescriptorLayout,
                                                                       this.textureSampler,
                                                                       this.defaultTexture,
                                                                       assetRoot);
        final SkeletalMeshLoader skeletalMeshLoader = new SkeletalMeshLoader(backend.deviceContext(),
                                                                             this.swapchain,
                                                                             this.descriptorPool,
                                                                             this.materialDescriptorLayout,
                                                                             this.boneDescriptorLayout,
                                                                             this.textureSampler,
                                                                             this.defaultTexture,
                                                                             assetRoot);
        this.staticMeshes = staticMeshLoader.load(Path.of("models/arena.obj"));
        this.humanoid = skeletalMeshLoader.load(Path.of("models/humanoid.fbx"));

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
        for (final var mesh : this.staticMeshes) {
            mesh.tryRecreate();
        }
        this.humanoid.tryRecreate();

        this.staticPipeline.tryRecreate();
        this.skeletalPipeline.tryRecreate();

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
                                  this.staticPipeline.getHandle());
                vkCmdBindDescriptorSets(commandBuffer.getHandle(),
                                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                                        this.staticPipeline.getLayout(),
                                        0,
                                        stack.longs(this.cameraUBO.getDescriptorSet(imageIndex),
                                                    this.sceneUBO.getDescriptorSet(imageIndex)),
                                        null);


                for (final var mesh : this.staticMeshes) {
                    mesh.draw(this.staticPipeline, commandBuffer, imageIndex);
                }


                vkCmdBindPipeline(commandBuffer.getHandle(),
                                  VK_PIPELINE_BIND_POINT_GRAPHICS,
                                  this.skeletalPipeline.getHandle());
                vkCmdBindDescriptorSets(commandBuffer.getHandle(),
                                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                                        this.skeletalPipeline.getLayout(),
                                        0,
                                        stack.longs(this.cameraUBO.getDescriptorSet(imageIndex),
                                                    this.sceneUBO.getDescriptorSet(imageIndex)),
                                        null);

                vkCmdBindDescriptorSets(commandBuffer.getHandle(),
                                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                                        this.skeletalPipeline.getLayout(),
                                        3,
                                        stack.longs(this.humanoid.getBoneDescriptorSet(imageIndex)),
                                        null);

                for (final var mesh : this.humanoid.getMeshes()) {
                    mesh.draw(this.skeletalPipeline, commandBuffer, imageIndex);
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
        this.boneDescriptorLayout.close();
        this.cameraDescriptorLayout.close();
        this.sceneDescriptorLayout.close();

        this.textureSampler.close();
        this.defaultTexture.close();
        for (final var mesh : this.staticMeshes) {
            mesh.close();
        }
        this.humanoid.close();

        this.depthTexture.close();

        this.framebuffers.close();
        this.renderPass.close();
        this.staticPipeline.close();
        this.skeletalPipeline.close();

        this.swapchain.close();
    }
}
