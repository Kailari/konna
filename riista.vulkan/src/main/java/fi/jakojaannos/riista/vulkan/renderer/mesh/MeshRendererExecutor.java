package fi.jakojaannos.riista.vulkan.renderer.mesh;

import fi.jakojaannos.riista.vulkan.rendering.RenderSubpass;
import fi.jakojaannos.riista.vulkan.util.RecreateCloseable;
import fi.jakojaannos.riista.vulkan.internal.RenderingBackend;
import fi.jakojaannos.riista.vulkan.internal.TextureSampler;
import fi.jakojaannos.riista.vulkan.application.PresentableState;
import fi.jakojaannos.riista.vulkan.internal.command.CommandBuffer;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorPool;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorSetLayout;
import fi.jakojaannos.riista.vulkan.internal.descriptor.SwapchainImageDependentDescriptorPool;
import fi.jakojaannos.riista.vulkan.internal.types.VkPrimitiveTopology;
import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.view.assets.Texture;
import fi.jakojaannos.riista.vulkan.CameraDescriptor;
import fi.jakojaannos.riista.vulkan.SceneUniformBufferObject;
import fi.jakojaannos.riista.vulkan.assets.material.MaterialDescriptor;
import fi.jakojaannos.riista.vulkan.assets.mesh.MeshImpl;
import fi.jakojaannos.riista.vulkan.assets.mesh.skeletal.AnimationDescriptor;
import fi.jakojaannos.riista.vulkan.assets.mesh.skeletal.SkeletalMeshImpl;
import fi.jakojaannos.riista.vulkan.assets.mesh.skeletal.SkeletalMeshVertex;
import fi.jakojaannos.riista.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.riista.vulkan.rendering.RenderPass;

import static fi.jakojaannos.riista.utilities.BitMask.bitMask;
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
            final RenderSubpass mainSubpass,
            final AssetManager assetManager,
            final DescriptorSetLayout cameraDescriptorLayout
    ) {
        // Buffers:     1 for material, 1 for scene, 1 for mesh
        // samplers:    1 for material
        this.descriptorPool = new SwapchainImageDependentDescriptorPool(
                backend,
                SETS_PER_IMAGE,
                bitMask(),
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
                                                       mainSubpass,
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
            final CameraDescriptor cameraUBO,
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


                this.animationDescriptor.setFrame(imageIndex,
                                                  entry.mesh.getAnimation(entry.animation),
                                                  entry.frame);
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
                                           stack.longs(((MeshImpl) subMesh).getVertexBuffer().getHandle()),
                                           stack.longs(0L));
                    vkCmdBindIndexBuffer(commandBuffer.getHandle(),
                                         ((MeshImpl) subMesh).getIndexBuffer().getHandle(),
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

        this.descriptorPool.close();

        this.textureSampler.close();
        this.materialDescriptor.close();
        this.sceneUBO.close();
        this.animationDescriptor.close();

        this.boneDescriptorLayout.close();
        this.materialDescriptorLayout.close();
        this.sceneDescriptorLayout.close();

        this.skeletalPipeline.close();
    }
}
