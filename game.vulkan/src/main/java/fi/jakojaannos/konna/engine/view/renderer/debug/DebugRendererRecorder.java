package fi.jakojaannos.konna.engine.view.renderer.debug;

import org.joml.Vector3f;

import fi.jakojaannos.konna.engine.application.PresentableState;
import fi.jakojaannos.konna.engine.view.DebugRenderer;
import fi.jakojaannos.konna.engine.view.Presentable;
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

    public static final class TransformEntry implements Presentable {
        public Vector3f position = new Vector3f();
        public float rotation;

        @Override
        public void reset() {
            this.position.set(0.0f);
            this.rotation = 0.0f;
        }
    }
}
