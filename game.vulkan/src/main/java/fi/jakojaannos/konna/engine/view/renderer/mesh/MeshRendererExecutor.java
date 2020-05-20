package fi.jakojaannos.konna.engine.view.renderer.mesh;

import fi.jakojaannos.konna.engine.CameraUniformBufferObject;
import fi.jakojaannos.konna.engine.SceneUniformBufferObject;
import fi.jakojaannos.konna.engine.application.PresentableState;
import fi.jakojaannos.konna.engine.assets.AssetManager;
import fi.jakojaannos.konna.engine.assets.Texture;
import fi.jakojaannos.konna.engine.assets.material.MaterialDescriptor;
import fi.jakojaannos.konna.engine.assets.mesh.AnimationDescriptor;
import fi.jakojaannos.konna.engine.assets.mesh.SkeletalMeshImpl;
import fi.jakojaannos.konna.engine.assets.mesh.SkeletalMeshVertex;
import fi.jakojaannos.konna.engine.util.RecreateCloseable;
import fi.jakojaannos.konna.engine.vulkan.RenderingBackend;
import fi.jakojaannos.konna.engine.vulkan.TextureSampler;
import fi.jakojaannos.konna.engine.vulkan.command.CommandBuffer;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.konna.engine.vulkan.descriptor.SwapchainImageDependentDescriptorPool;
import fi.jakojaannos.konna.engine.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.konna.engine.vulkan.rendering.RenderPass;
import fi.jakojaannos.konna.engine.vulkan.types.VkPrimitiveTopology;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class MeshRendererExecutor extends RecreateCloseable {
    /**
     * Number of descriptor sets used for materials. e.g. material colors, textures, etc.
     */
    private static final int MATERIAL_SET_COUNT = 2;

    /**
     * Number of descriptor sets used for mesh information. e.g. bones etc.
     */
    private static final int MESH_SET_COUNT = 1;

    /**
     * Number of descriptor sets used for scene information. e.g. lighting etc.
     */
    private static final int SCENE_SET_COUNT = 1;

    private static final int SETS_PER_IMAGE = MATERIAL_SET_COUNT + MESH_SET_COUNT + SCENE_SET_COUNT;

    private final DescriptorSetLayout materialDescriptorLayout;
    private final DescriptorSetLayout boneDescriptorLayout;
    private final DescriptorSetLayout sceneDescriptorLayout;
    private final DescriptorPool descriptorPool;

    private final TextureSampler textureSampler;
    private final MaterialDescriptor materialDescriptor;
    private final AnimationDescriptor animationDescriptor;
    private final SceneUniformBufferObject sceneUBO;

    private final GraphicsPipeline<SkeletalMeshVertex> skeletalPipeline;

    public MeshRendererExecutor(
            final RenderingBackend backend,
            final RenderPass renderPass,
            final AssetManager assetManager,
            final DescriptorSetLayout cameraDescriptorLayout
    ) {
        // Buffers:     1 for material, 1 for scene, 1 for mesh
        // samplers:    1 for material
        this.descriptorPool = new SwapchainImageDependentDescriptorPool(
                backend.deviceContext(),
                backend.swapchain(),
                SETS_PER_IMAGE,
                new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                        () -> backend.swapchain()
                                                     .getImageCount() * (1 + 1 + 1)),
                new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                                        backend.swapchain()::getImageCount));

        this.materialDescriptorLayout = new DescriptorSetLayout(backend.deviceContext(),
                                                                MaterialDescriptor.TEXTURE_DESCRIPTOR_BINDING,
                                                                MaterialDescriptor.MATERIAL_DESCRIPTOR_BINDING);
        this.boneDescriptorLayout = new DescriptorSetLayout(backend.deviceContext(),
                                                            SkeletalMeshImpl.BONE_DESCRIPTOR_BINDING);

        this.sceneDescriptorLayout = new DescriptorSetLayout(backend.deviceContext(),
                                                             SceneUniformBufferObject.LIGHTS_DESCRIPTOR_BINDING,
                                                             SceneUniformBufferObject.LIGHT_COUNT_DESCRIPTOR_BINDING);

        this.sceneUBO = new SceneUniformBufferObject(backend.deviceContext(),
                                                     backend.swapchain(),
                                                     this.descriptorPool,
                                                     this.sceneDescriptorLayout);

        this.textureSampler = new TextureSampler(backend.deviceContext());

        final var defaultTexture = assetManager.getStorage(Texture.class)
                                               .getOrDefault("textures/vulkan/texture.jpg");
        this.materialDescriptor = new MaterialDescriptor(backend.deviceContext(),
                                                         backend.swapchain(),
                                                         defaultTexture,
                                                         this.descriptorPool,
                                                         this.materialDescriptorLayout,
                                                         this.textureSampler);
        this.animationDescriptor = new AnimationDescriptor(backend,
                                                           this.descriptorPool,
                                                           this.boneDescriptorLayout);

        this.skeletalPipeline = new GraphicsPipeline<>(backend.deviceContext(),
                                                       backend.swapchain(),
                                                       renderPass,
                                                       assetManager,
                                                       "shaders/vulkan/skeletalMesh.vert",
                                                       "shaders/vulkan/shader.frag",
                                                       VkPrimitiveTopology.TRIANGLE_LIST,
                                                       SkeletalMeshVertex.FORMAT,
                                                       cameraDescriptorLayout,
                                                       this.sceneUBO.getLayout(),
                                                       this.materialDescriptorLayout,
                                                       this.boneDescriptorLayout);
    }

    public void flush(
            final PresentableState state,
            final CameraUniformBufferObject cameraUBO,
            final CommandBuffer commandBuffer,
            final int imageIndex
    ) {
        try (final var stack = stackPush()) {
            final var pushConstantData = stack.malloc(16 * Float.BYTES);

            vkCmdBindPipeline(commandBuffer.getHandle(),
                              VK_PIPELINE_BIND_POINT_GRAPHICS,
                              this.skeletalPipeline.getHandle());

            vkCmdBindDescriptorSets(commandBuffer.getHandle(),
                                    VK_PIPELINE_BIND_POINT_GRAPHICS,
                                    this.skeletalPipeline.getLayout(),
                                    0,
                                    stack.longs(cameraUBO.getDescriptorSet(imageIndex),
                                                this.sceneUBO.getDescriptorSet(imageIndex)),
                                    null);

            for (final var entry : state.skeletalMeshEntries()) {
                entry.transform.get(0, pushConstantData);
                vkCmdPushConstants(commandBuffer.getHandle(),
                                   this.skeletalPipeline.getLayout(),
                                   VK_SHADER_STAGE_VERTEX_BIT,
                                   0,
                                   pushConstantData);

                entry.mesh.setFrame(this.animationDescriptor, imageIndex, entry.animation, entry.frame);
                vkCmdBindDescriptorSets(commandBuffer.getHandle(),
                                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                                        this.skeletalPipeline.getLayout(),
                                        3,
                                        stack.longs(this.animationDescriptor.getDescriptorSet(imageIndex)),
                                        null);

                for (final var subMesh : entry.mesh) {
                    this.materialDescriptor.update(subMesh.getMaterial(), imageIndex);

                    vkCmdBindDescriptorSets(commandBuffer.getHandle(),
                                            VK_PIPELINE_BIND_POINT_GRAPHICS,
                                            this.skeletalPipeline.getLayout(),
                                            2,
                                            stack.longs(this.materialDescriptor.getDescriptorSet(imageIndex)),
                                            null);

                    vkCmdBindVertexBuffers(commandBuffer.getHandle(),
                                           0,
                                           stack.longs(subMesh.getVertexBuffer().getHandle()),
                                           stack.longs(0L));
                    vkCmdBindIndexBuffer(commandBuffer.getHandle(),
                                         subMesh.getIndexBuffer().getHandle(),
                                         0,
                                         VK_INDEX_TYPE_UINT32);

                    vkCmdDrawIndexed(commandBuffer.getHandle(),
                                     subMesh.getIndexCount(),
                                     1,
                                     0,
                                     0,
                                     0);
                }
            }
        }
    }

    @Override
    protected void recreate() {
        this.descriptorPool.tryRecreate();
        this.materialDescriptor.tryRecreate();
        this.animationDescriptor.tryRecreate();
        this.sceneUBO.tryRecreate();

        this.skeletalPipeline.tryRecreate();
    }

    @Override
    protected void cleanup() {
    }

    @Override
    public void close() {
        super.close();

        this.textureSampler.close();
        this.materialDescriptor.close();
        this.sceneUBO.close();
        this.animationDescriptor.close();

        this.boneDescriptorLayout.close();
        this.materialDescriptorLayout.close();
        this.sceneDescriptorLayout.close();

        this.descriptorPool.close();
        this.skeletalPipeline.close();
    }
}
