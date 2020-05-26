package fi.jakojaannos.konna.engine.assets.mesh.skeletal;

import java.util.*;

import fi.jakojaannos.konna.engine.assets.Mesh;
import fi.jakojaannos.konna.engine.assets.SkeletalMesh;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorBinding;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

public class SkeletalMeshImpl implements SkeletalMesh {
    public static final DescriptorBinding BONE_DESCRIPTOR_BINDING = new DescriptorBinding(0,
                                                                                          VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                                                                                          1,
                                                                                          VK_SHADER_STAGE_VERTEX_BIT);
    private final Collection<Mesh> submeshes;
    private final Map<String, Animation> animations;

    public SkeletalMeshImpl(
            final Map<String, Animation> animations,
            final Mesh... submeshes
    ) {
        this.submeshes = List.of(submeshes);

        this.animations = Collections.unmodifiableMap(animations);
    }

    @Override
    public void setFrame(
            final AnimationDescriptor animationDescriptor,
            final int imageIndex,
            final String animation,
            final int frame
    ) {
        animationDescriptor.setFrame(imageIndex,
                                     this.animations.get(animation),
                                     frame);
    }

    @Override
    public Iterator<Mesh> iterator() {
        return this.submeshes.iterator();
    }

    @Override
    public void close() {
        this.submeshes.forEach(Mesh::close);
    }
}
