package fi.jakojaannos.konna.engine.vulkan;

import java.nio.file.Path;

import fi.jakojaannos.konna.engine.CameraUniformBufferObject;
import fi.jakojaannos.konna.engine.application.PresentableState;
import fi.jakojaannos.konna.engine.view.renderer.RendererExecutor;
import fi.jakojaannos.konna.engine.vulkan.rendering.Swapchain;
import fi.jakojaannos.konna.engine.vulkan.window.Window;

public class RenderingContext implements AutoCloseable {
    private final Swapchain swapchain;
    private final RendererExecutor renderer;

    /*private final GraphicsPipeline<StaticMeshVertex> staticPipeline;
    private final GraphicsPipeline<SkeletalMeshVertex> skeletalPipeline;
    private final DescriptorSetLayout materialDescriptorLayout;
    private final DescriptorSetLayout boneDescriptorLayout;
    private final DescriptorSetLayout sceneDescriptorLayout;
    private final SceneUniformBufferObject sceneUBO;
    private final TextureSampler textureSampler;
    private final Texture defaultTexture;

    private final StaticMesh[] staticMeshes;
    private final AnimatedMesh humanoid;*/

    public int getSwapchainImageCount() {
        return this.swapchain.getImageCount();
    }

    public Swapchain getSwapchain() {
        return this.swapchain;
    }

    /*
    public AnimatedMesh getHumanoid() {
        return this.humanoid;
    }
     */

    @Deprecated
    public CameraUniformBufferObject getCameraUBO() {
        return this.renderer.getCameraUBO();
    }

    /*
    public void recordFrame(final int imageIndex, final PresentableState state) {
        final var commandBuffer = this.commandBuffers[imageIndex];
        final var framebuffer = this.framebuffers.get(imageIndex);

        try (final var stack = stackPush();
             final var ignored = commandBuffer.begin();
             final var ignored2 = this.renderPass.begin(framebuffer, commandBuffer)
        ) {
            final var pushConstantData = stack.malloc(16 * Float.BYTES);
            final var modelMatrix = new Matrix4f();

            vkCmdBindPipeline(commandBuffer.getHandle(),
                              VK_PIPELINE_BIND_POINT_GRAPHICS,
                              this.staticPipeline.getHandle());
            modelMatrix.get(0, pushConstantData);

            vkCmdPushConstants(commandBuffer.getHandle(),
                               this.skeletalPipeline.getLayout(),
                               VK_SHADER_STAGE_VERTEX_BIT,
                               0,
                               pushConstantData);

            // NOTE: This needs to be done again for the skeletal pipeline if descriptor sets
            //       and/or push constant ranges are changed so that they become incompatible
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

            // XXX: Re-bind the scene/camera UBOs here if necessary (see comment above)

            vkCmdBindDescriptorSets(commandBuffer.getHandle(),
                                    VK_PIPELINE_BIND_POINT_GRAPHICS,
                                    this.skeletalPipeline.getLayout(),
                                    3,
                                    stack.longs(this.humanoid.getBoneDescriptorSet(imageIndex)),
                                    null);

            for (final var entity : state.positions()) {
                modelMatrix.identity()
                           .translate((float) entity.x, (float) entity.y, 0.0f);
                modelMatrix.get(0, pushConstantData);

                vkCmdPushConstants(commandBuffer.getHandle(),
                                   this.skeletalPipeline.getLayout(),
                                   VK_SHADER_STAGE_VERTEX_BIT,
                                   0,
                                   pushConstantData);

                for (final var mesh : this.humanoid.getMeshes()) {
                    mesh.draw(this.skeletalPipeline, commandBuffer, imageIndex);
                }
            }
        }
    }
     */

    public RenderingContext(final Path assetRoot, final RenderingBackend backend, final Window window) {
        this.swapchain = new Swapchain(backend.deviceContext(), window, backend.surface());

        this.renderer = new RendererExecutor(backend.deviceContext(), this.swapchain, assetRoot);

        /*
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
        final var staticMeshLoader = new StaticMeshLoader(backend.deviceContext(),
                                                          this.swapchain,
                                                          this.descriptorPool,
                                                          this.materialDescriptorLayout,
                                                          this.textureSampler,
                                                          this.defaultTexture,
                                                          assetRoot);
        final var skeletalMeshLoader = new SkeletalMeshLoader(backend.deviceContext(),
                                                              this.swapchain,
                                                              this.descriptorPool,
                                                              this.materialDescriptorLayout,
                                                              this.boneDescriptorLayout,
                                                              this.textureSampler,
                                                              this.defaultTexture,
                                                              assetRoot);
        this.staticMeshes = staticMeshLoader.load(Path.of("models/arena.obj"));
        this.humanoid = skeletalMeshLoader.load(Path.of("models/humanoid.fbx"));
         */

        recreateSwapchain();
    }

    public void recreateSwapchain() {
        this.swapchain.tryRecreate();

        this.renderer.tryRecreate();
    }

    @Override
    public void close() {
        this.renderer.close();
        this.swapchain.close();
    }

    public void recordFrame(final int imageIndex, final PresentableState state) {
        this.renderer.flush(state, imageIndex);
    }

    public void submit(
            final int imageIndex,
            final long fence,
            final long imageAvailableSemaphore,
            final long renderFinishedSemaphore
    ) {
        this.renderer.submit(imageIndex, fence, imageAvailableSemaphore, renderFinishedSemaphore);
    }
}
