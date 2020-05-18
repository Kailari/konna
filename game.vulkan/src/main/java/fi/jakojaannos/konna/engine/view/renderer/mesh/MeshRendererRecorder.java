package fi.jakojaannos.konna.engine.view.renderer.mesh;

import fi.jakojaannos.konna.engine.application.PresentableState;
import fi.jakojaannos.konna.engine.assets.mesh.SkeletalMesh;
import fi.jakojaannos.konna.engine.view.MeshRenderer;
import fi.jakojaannos.roguelite.engine.data.components.Transform;

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
        // TODO
    }
}
