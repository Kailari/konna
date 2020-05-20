package fi.jakojaannos.konna.engine.assets;

import java.util.Map;

import fi.jakojaannos.konna.engine.assets.mesh.Animation;
import fi.jakojaannos.konna.engine.assets.mesh.AnimationDescriptor;
import fi.jakojaannos.konna.engine.assets.mesh.SkeletalMeshImpl;

public interface SkeletalMesh extends Iterable<Mesh>, AutoCloseable {
    static SkeletalMesh from(final Map<String, Animation> animations, final Mesh[] submeshes) {
        return new SkeletalMeshImpl(animations, submeshes);
    }

    void setFrame(
            AnimationDescriptor animationDescriptor,
            int imageIndex,
            String animation,
            int frame
    );

    @Override
    void close();
}
