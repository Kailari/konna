package fi.jakojaannos.riista.vulkan.renderer.mesh;

import org.joml.Matrix4f;

import fi.jakojaannos.riista.vulkan.application.PresentableState;
import fi.jakojaannos.riista.view.assets.SkeletalMesh;
import fi.jakojaannos.riista.view.MeshRenderer;
import fi.jakojaannos.riista.view.Presentable;
import fi.jakojaannos.riista.vulkan.assets.mesh.skeletal.SkeletalMeshImpl;
import fi.jakojaannos.riista.data.components.Transform;

public class MeshRendererRecorder implements MeshRenderer {
    private PresentableState state;

    public void setWriteState(final PresentableState state) {
        this.state = state;
    }

    @Override
    public void drawSkeletal(
            final Transform transform,
            final SkeletalMesh mesh,
            final String animation,
            final int frame
    ) {
        final var entry = this.state.skeletalMeshEntries().get();

        entry.transform.translate((float) transform.position.x,
                                  (float) transform.position.y,
                                  0.0f)
                       .rotateZ((float) transform.rotation);
        entry.mesh = (SkeletalMeshImpl) mesh;
        entry.animation = animation;
        entry.frame = frame;
    }

    public static final class SkeletalEntry implements Presentable {
        public Matrix4f transform = new Matrix4f();
        public SkeletalMeshImpl mesh;
        public String animation;
        public int frame;

        @Override
        public void reset() {
            this.transform.identity();
            this.mesh = null;
            this.frame = 0;
            this.animation = "idle";
        }
    }
}
