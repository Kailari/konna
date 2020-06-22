package fi.jakojaannos.riista.vulkan.renderer.mesh;

import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.view.assets.Texture;
import fi.jakojaannos.riista.vulkan.CameraDescriptor;
import fi.jakojaannos.riista.vulkan.SceneUniformBufferObject;
import fi.jakojaannos.riista.vulkan.application.PresentableState;
import fi.jakojaannos.riista.vulkan.assets.material.MaterialDescriptor;
import fi.jakojaannos.riista.vulkan.assets.mesh.MaterialDescriptorPool;
import fi.jakojaannos.riista.vulkan.assets.mesh.MeshImpl;
import fi.jakojaannos.riista.vulkan.assets.mesh.skeletal.AnimationDescriptorPool;
import fi.jakojaannos.riista.vulkan.assets.mesh.skeletal.SkeletalMeshImpl;
import fi.jakojaannos.riista.vulkan.assets.mesh.skeletal.SkeletalMeshVertex;
import fi.jakojaannos.riista.vulkan.assets.mesh.staticmesh.StaticMeshVertex;
import fi.jakojaannos.riista.vulkan.internal.RenderingBackend;
import fi.jakojaannos.riista.vulkan.internal.TextureSampler;
import fi.jakojaannos.riista.vulkan.internal.command.CommandBuffer;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorPool;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorSetLayout;
import fi.jakojaannos.riista.vulkan.internal.descriptor.SwapchainImageDependentDescriptorPool;
import fi.jakojaannos.riista.vulkan.internal.types.VkDescriptorPoolCreateFlags;
import fi.jakojaannos.riista.vulkan.internal.types.VkPrimitiveTopology;
import fi.jakojaannos.riista.vulkan.rendering.GraphicsPipeline;
import fi.jakojaannos.riista.vulkan.rendering.RenderPass;
import fi.jakojaannos.riista.vulkan.rendering.RenderSubpass;
import fi.jakojaannos.riista.vulkan.util.RecreateCloseable;

import static fi.jakojaannos.riista.utilities.BitMask.bitMask;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class MeshRendererExecutor extends RecreateCloseable {
    private final RenderingBackend backend;

    private final DescriptorSetLayout materialDescriptorLayout;
    private final DescriptorSetLayout boneDescriptorLayout;
    private final DescriptorSetLayout sceneDescriptorLayout;
    private final DescriptorPool descriptorPool;

    private final GraphicsPipeline<SkeletalMeshVertex> skeletalPipeline;
    private final GraphicsPipeline<StaticMeshVertex> staticPipeline;

    private final TextureSampler textureSampler;
    private final Texture defaultTexture;

    private final SceneUniformBufferObject sceneUBO;


    private int swapchainImageCount;
    private AnimationDescriptorPool[] animationDescriptors;
    private MaterialDescriptorPool[] materialDescriptors;

    public MeshRendererExecutor(
            final RenderingBackend backend,
            final RenderPass renderPass,
            final RenderSubpass mainSubpass,
            final AssetManager assetManager,
            final DescriptorSetLayout cameraDescriptorLayout
    ) {
        this.backend = backend;

        // Buffers:     2 for materials, 1 for scene, 1 for mesh
        // samplers:    2 for material
        this.descriptorPool = new SwapchainImageDependentDescriptorPool(
                backend,
                1,
                bitMask(VkDescriptorPoolCreateFlags.FREE_DESCRIPTOR_SET_BIT),
                new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                        () -> backend.swapchain().getImageCount()));

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

        this.defaultTexture = assetManager.getStorage(Texture.class)
                                          .getOrDefault("textures/vulkan/texture.jpg");


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
        this.staticPipeline = new GraphicsPipeline<>(backend.deviceContext(),
                                                     backend.swapchain(),
                                                     renderPass,
                                                     mainSubpass,
                                                     assetManager,
                                                     "shaders/vulkan/staticMesh.vert",
                                                     "shaders/vulkan/shader.frag",
                                                     VkPrimitiveTopology.TRIANGLE_LIST,
                                                     StaticMeshVertex.FORMAT,
                                                     cameraDescriptorLayout,
                                                     this.sceneUBO.getLayout(),
                                                     this.materialDescriptorLayout);
    }

    public void flush(
            final PresentableState state,
            final CameraDescriptor cameraUBO,
            final CommandBuffer commandBuffer,
            final int imageIndex
    ) {
        this.animationDescriptors[imageIndex].reset();
        this.materialDescriptors[imageIndex].reset();

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

                final var animationDescriptor = this.animationDescriptors[imageIndex].get(entry.mesh,
                                                                                          entry.animation,
                                                                                          entry.frame);
                vkCmdBindDescriptorSets(commandBuffer.getHandle(),
                                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                                        this.skeletalPipeline.getLayout(),
                                        3,
                                        stack.longs(animationDescriptor.getDescriptorSet(0)),
                                        null);

                for (final var subMesh : entry.mesh) {
                    // FIXME: Do similar thing here that is done with bones/animations
                    final var materialDescriptor = this.materialDescriptors[imageIndex].get(subMesh.getMaterial());
                    vkCmdBindDescriptorSets(commandBuffer.getHandle(),
                                            VK_PIPELINE_BIND_POINT_GRAPHICS,
                                            this.skeletalPipeline.getLayout(),
                                            2,
                                            stack.longs(materialDescriptor.getDescriptorSet(0)),
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

            vkCmdBindPipeline(commandBuffer.getHandle(),
                              VK_PIPELINE_BIND_POINT_GRAPHICS,
                              this.staticPipeline.getHandle());

            vkCmdBindDescriptorSets(commandBuffer.getHandle(),
                                    VK_PIPELINE_BIND_POINT_GRAPHICS,
                                    this.staticPipeline.getLayout(),
                                    0,
                                    stack.longs(cameraUBO.getDescriptorSet(imageIndex),
                                                this.sceneUBO.getDescriptorSet(imageIndex)),
                                    null);

            for (final var entry : state.staticMeshEntries()) {
                entry.transform.get(0, pushConstantData);
                vkCmdPushConstants(commandBuffer.getHandle(),
                                   this.staticPipeline.getLayout(),
                                   VK_SHADER_STAGE_VERTEX_BIT,
                                   0,
                                   pushConstantData);

                for (final var subMesh : entry.mesh) {
                    final var materialDescriptor = this.materialDescriptors[imageIndex].get(subMesh.getMaterial());

                    vkCmdBindDescriptorSets(commandBuffer.getHandle(),
                                            VK_PIPELINE_BIND_POINT_GRAPHICS,
                                            this.staticPipeline.getLayout(),
                                            2,
                                            stack.longs(materialDescriptor.getDescriptorSet(0)),
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
        if (this.swapchainImageCount != this.backend.swapchain().getImageCount()) {
            this.swapchainImageCount = this.backend.swapchain().getImageCount();

            this.animationDescriptors = new AnimationDescriptorPool[this.swapchainImageCount];
            this.materialDescriptors = new MaterialDescriptorPool[this.swapchainImageCount];
            for (int i = 0; i < this.swapchainImageCount; i++) {
                this.animationDescriptors[i] = new AnimationDescriptorPool(this.backend,
                                                                           this.boneDescriptorLayout);
                this.materialDescriptors[i] = new MaterialDescriptorPool(this.backend,
                                                                         this.defaultTexture,
                                                                         this.textureSampler,
                                                                         this.materialDescriptorLayout);
            }
        } else {
            for (int i = 0; i < this.swapchainImageCount; i++) {
                this.animationDescriptors[i].tryRecreate();
                this.materialDescriptors[i].tryRecreate();
            }
        }

        this.descriptorPool.tryRecreate();
        this.sceneUBO.tryRecreate();

        this.skeletalPipeline.tryRecreate();
        this.staticPipeline.tryRecreate();
    }

    @Override
    protected void cleanup() {
        if (this.swapchainImageCount != this.backend.swapchain().getImageCount()) {
            for (int i = 0; i < this.swapchainImageCount; i++) {
                this.animationDescriptors[i].close();
                this.materialDescriptors[i].close();
            }
        }
    }

    @Override
    public void close() {
        super.close();

        this.descriptorPool.close();

        this.textureSampler.close();
        this.sceneUBO.close();
        for (int i = 0; i < this.swapchainImageCount; i++) {
            this.animationDescriptors[i].close();
            this.materialDescriptors[i].close();
        }

        this.boneDescriptorLayout.close();
        this.materialDescriptorLayout.close();
        this.sceneDescriptorLayout.close();

        this.skeletalPipeline.close();
        this.staticPipeline.close();
    }
}
