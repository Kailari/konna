package fi.jakojaannos.riista.vulkan.assets.mesh.skeletal;

import org.joml.Matrix4f;

import java.nio.ByteBuffer;
import java.util.List;

import javax.annotation.Nullable;

import fi.jakojaannos.riista.vulkan.internal.RenderingBackend;
import fi.jakojaannos.riista.vulkan.internal.descriptor.*;
import fi.jakojaannos.riista.vulkan.internal.device.DeviceContext;
import fi.jakojaannos.riista.vulkan.rendering.Swapchain;

public class AnimationDescriptor extends DescriptorObject {
    private static final int MAX_BONES = 150;

    private final BoneBinding boneBinding;

    private final Animation identity;

    public AnimationDescriptor(
            final RenderingBackend backend,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout
    ) {
        this(backend.deviceContext(),
             backend.swapchain(),
             descriptorPool,
             layout,
             new BoneBinding());
    }

    private AnimationDescriptor(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout layout,
            final BoneBinding boneBinding
    ) {
        super(deviceContext,
              swapchain,
              descriptorPool,
              layout,
              new CombinedImageSamplerBinding[0],
              new UniformBufferBinding[]{
                      boneBinding
              });

        this.identity = createIdentityAnimation();
        this.boneBinding = boneBinding;
    }

    public void setFrame(final int imageIndex, @Nullable final Animation animation, final int frame) {
        if (animation != null) {
            this.boneBinding.active = animation;
            this.boneBinding.frame = frame % animation.frames().size();
        } else {
            this.boneBinding.active = this.identity;
            this.boneBinding.frame = 0;
        }

        flushAllUniformBufferBindings(imageIndex);
    }

    private static Animation createIdentityAnimation() {
        final var boneTransforms = new Matrix4f[MAX_BONES];
        for (int i = 0; i < boneTransforms.length; i++) {
            boneTransforms[i] = new Matrix4f().identity();
        }

        return new Animation("idle",
                             List.of(new Animation.Frame(boneTransforms)),
                             Double.POSITIVE_INFINITY);
    }

    private static class BoneBinding implements UniformBufferBinding {
        private Animation active;
        private int frame;

        public BoneBinding() {
            this.active = createIdentityAnimation();
        }

        @Override
        public int binding() {
            return SkeletalMeshImpl.BONE_DESCRIPTOR_BINDING.slot();
        }

        @Override
        public long sizeInBytes() {
            return 16 * Float.BYTES * MAX_BONES;
        }

        @Override
        public void write(final int offset, final ByteBuffer buffer) {
            final var frame = this.active.frames().get(this.frame);
            for (int i = 0; i < frame.boneTransforms().length; i++) {
                final var matrix = frame.boneTransforms()[i];

                final var boneOffset = i * 16 * Float.BYTES;
                matrix.get(offset + boneOffset, buffer);
            }
        }
    }
}
