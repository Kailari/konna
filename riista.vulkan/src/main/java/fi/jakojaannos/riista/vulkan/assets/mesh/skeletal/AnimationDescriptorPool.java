package fi.jakojaannos.riista.vulkan.assets.mesh.skeletal;

import fi.jakojaannos.riista.view.assets.SkeletalMesh;
import fi.jakojaannos.riista.vulkan.internal.RenderingBackend;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorObjectPool;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorPool;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorSetLayout;
import fi.jakojaannos.riista.vulkan.internal.types.VkDescriptorPoolCreateFlags;

import static fi.jakojaannos.riista.utilities.BitMask.bitMask;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;

public class AnimationDescriptorPool extends DescriptorObjectPool<AnimationDescriptor, AnimationDescriptorPool.Key> {
    private static final int MAX_SIMULTANEOUS_ANIMATIONS = 32;

    public AnimationDescriptorPool(
            final RenderingBackend backend,
            final DescriptorSetLayout descriptorLayout
    ) {
        super(new DescriptorPool(backend.deviceContext(),
                                 () -> MAX_SIMULTANEOUS_ANIMATIONS,
                                 bitMask(VkDescriptorPoolCreateFlags.FREE_DESCRIPTOR_SET_BIT),
                                 new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                         () -> MAX_SIMULTANEOUS_ANIMATIONS)) {
                  @Override
                  protected boolean isRecreateRequired() {
                      return false;
                  }
              },
              (descriptorPool) -> new AnimationDescriptor(backend,
                                                          descriptorPool,
                                                          descriptorLayout),
              (key, descriptor) -> descriptor.setFrame(key.mesh.getAnimation(key.animation),
                                                       key.frame),
              MAX_SIMULTANEOUS_ANIMATIONS);
    }

    public AnimationDescriptor get(
            final SkeletalMesh mesh,
            final String animation,
            final int frame
    ) {
        return get(new Key(mesh, animation, frame));
    }

    public static record Key(SkeletalMesh mesh, String animation, int frame) {}
}
