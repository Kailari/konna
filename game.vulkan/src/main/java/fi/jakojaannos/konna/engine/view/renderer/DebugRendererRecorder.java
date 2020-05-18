package fi.jakojaannos.konna.engine.view.renderer;

import fi.jakojaannos.konna.engine.application.PresentableState;
import fi.jakojaannos.konna.engine.view.DebugRenderer;
import fi.jakojaannos.roguelite.engine.data.components.Transform;

public class DebugRendererRecorder implements DebugRenderer {
    private PresentableState writeState;

    public void setWriteState(final PresentableState state) {
        this.writeState = state;
    }

    @Override
    public void drawTransform(final Transform transform) {
        final var entry = this.writeState.transforms().get();
        entry.position.set(transform.position.x,
                           transform.position.y,
                           0.0d);
        entry.rotation = (float) transform.rotation;
    }
}
