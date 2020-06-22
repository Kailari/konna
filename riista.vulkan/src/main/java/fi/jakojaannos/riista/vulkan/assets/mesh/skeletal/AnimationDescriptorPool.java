package fi.jakojaannos.riista.vulkan.assets.mesh.skeletal;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fi.jakojaannos.riista.view.assets.SkeletalMesh;
import fi.jakojaannos.riista.vulkan.internal.RenderingBackend;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorPool;
import fi.jakojaannos.riista.vulkan.internal.descriptor.DescriptorSetLayout;
import fi.jakojaannos.riista.vulkan.internal.types.VkDescriptorPoolCreateFlags;
import fi.jakojaannos.riista.vulkan.util.RecreateCloseable;

import static fi.jakojaannos.riista.utilities.BitMask.bitMask;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;

public class AnimationDescriptorPool extends RecreateCloseable {
    private static final int MAX_SIMULTANEOUS_ANIMATIONS = 32;

    private final DescriptorPool descriptorPool;

    private final Collection<AnimationDescriptor> allDescriptors;

    private final Queue<AnimationDescriptor> freeDescriptors;
    private final Map<Key, AnimationDescriptor> descriptors;

    public AnimationDescriptorPool(
            final RenderingBackend backend,
            final DescriptorSetLayout descriptorLayout
    ) {
        this.descriptorPool =
                new DescriptorPool(backend.deviceContext(),
                                   () -> MAX_SIMULTANEOUS_ANIMATIONS,
                                   bitMask(VkDescriptorPoolCreateFlags.FREE_DESCRIPTOR_SET_BIT),
                                   new DescriptorPool.Pool(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                           () -> MAX_SIMULTANEOUS_ANIMATIONS)) {
                    @Override
                    protected boolean isRecreateRequired() {
                        return false;
                    }
                };

        this.allDescriptors = Stream.generate(() -> new AnimationDescriptor(backend,
                                                                            this.descriptorPool,
                                                                            descriptorLayout))
                                    .limit(MAX_SIMULTANEOUS_ANIMATIONS)
                                    .collect(Collectors.toUnmodifiableList());
        this.freeDescriptors = new ArrayDeque<>(this.allDescriptors);
        this.descriptors = new HashMap<>(this.freeDescriptors.size());
    }

    public void reset() {
        // FIXME: Benchmark if moving to intermediate "cachedDescriptors"-map could bring better perf
        //  - idea is to assume same descriptors are often re-used between frames
        //  - free descriptors are consumed first
        //  - if no more free descriptors are available, cache is free for re-use. Additional
        //    indirection here may impact perf negatively
        //  - if matching key is found from cache, cached version can directly be used without need
        //    of uploading data (move cache -> descriptors). This should be where the perf increases
        this.freeDescriptors.addAll(this.descriptors.values());
        this.descriptors.clear();
    }

    public AnimationDescriptor get(
            final SkeletalMesh mesh,
            final String animation,
            final int frame
    ) {
        final var key = new Key(mesh, animation, frame);
        return this.descriptors.computeIfAbsent(key, this::createDescriptor);
    }

    private AnimationDescriptor createDescriptor(final Key key) {
        // FIXME: Handle exhausted pool gracefully instead of crashing
        final var descriptor = this.freeDescriptors.remove();
        descriptor.setFrame(key.mesh.getAnimation(key.animation),
                            key.frame);

        return descriptor;
    }

    @Override
    protected void recreate() {
        reset();
        this.descriptorPool.tryRecreate();
        this.allDescriptors.forEach(RecreateCloseable::tryRecreate);
    }

    @Override
    protected void cleanup() {
    }

    @Override
    public void close() {
        super.close();
        this.descriptors.clear();
        this.freeDescriptors.clear();
        this.descriptorPool.close();
        this.allDescriptors.forEach(RecreateCloseable::close);
    }

    private static record Key(SkeletalMesh mesh, String animation, int frame) {}
}
