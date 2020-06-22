package fi.jakojaannos.riista.vulkan.assets.mesh;

import fi.jakojaannos.riista.view.assets.Material;
import fi.jakojaannos.riista.view.assets.Texture;
import fi.jakojaannos.riista.vulkan.assets.material.MaterialDescriptor;
import fi.jakojaannos.riista.vulkan.internal.RenderingBackend;
import fi.jakojaannos.riista.vulkan.internal.TextureSampler;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorObjectPool;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorPool;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorSetLayout;
import fi.jakojaannos.riista.vulkan.internal.types.VkDescriptorPoolCreateFlags;

import static fi.jakojaannos.riista.utilities.BitMask.bitMask;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;

public class MaterialDescriptorPool extends DescriptorObjectPool<MaterialDescriptor, MaterialDescriptorPool.Key> {
    private static final int MAX_SIMULTANEOUS_MATERIALS = 64;

    public MaterialDescriptorPool(
            final RenderingBackend backend,
            final Texture defaultTexture,
            final TextureSampler textureSampler,
            final DescriptorSetLayout descriptorLayout
    ) {
        super(new DescriptorPool(backend.deviceContext(),
                                 () -> MAX_SIMULTANEOUS_MATERIALS * 2,
                                 bitMask(VkDescriptorPoolCreateFlags.FREE_DESCRIPTOR_SET_BIT),
                                 new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                         () -> MAX_SIMULTANEOUS_MATERIALS),
                                 new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                                                         () -> MAX_SIMULTANEOUS_MATERIALS)) {
                  @Override
                  protected boolean isRecreateRequired() {
                      return false;
                  }
              },
              (descriptorPool) -> new MaterialDescriptor(backend,
                                                         defaultTexture,
                                                         descriptorPool,
                                                         descriptorLayout,
                                                         textureSampler),
              ((key, descriptor) -> descriptor.update(key.material)),
              MAX_SIMULTANEOUS_MATERIALS);
    }

    public MaterialDescriptor get(final Material material) {
        return get(new Key(material));
    }

    public static record Key(Material material) {}
}
