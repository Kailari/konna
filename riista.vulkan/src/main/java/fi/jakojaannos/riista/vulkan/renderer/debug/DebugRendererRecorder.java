package fi.jakojaannos.riista.vulkan.renderer.debug;

import org.joml.Vector2f;
import org.joml.Vector3f;

import fi.jakojaannos.riista.vulkan.application.PresentableState;
import fi.jakojaannos.riista.view.DebugRenderer;
import fi.jakojaannos.riista.view.Presentable;
import fi.jakojaannos.riista.data.components.Transform;

public class DebugRendererRecorder implements DebugRenderer {
    private PresentableState writeState;

    public void setWriteState(final PresentableState state) {
        this.writeState = state;
    }

    @Override
    public void drawTransform(final Transform transform) {
        final var entry = this.writeState.transformEntries().get();
        entry.position.set(transform.position.x,
                           transform.position.y,
                           0.0d);
        entry.rotation = (float) transform.rotation;
    }

    @Override
    public void drawBox(final Transform transform, final Vector2f offset, final Vector2f size) {
        final var entry = this.writeState.boxEntries().get();
        entry.position.set(transform.position.x,
                           transform.position.y,
                           0.0d);
        entry.rotation = (float) transform.rotation;

        entry.offset.set(offset);
        entry.size.set(size);
    }

    public static final class TransformEntry implements Presentable {
        public final Vector3f position = new Vector3f();
        public float rotation;

        @Override
        public void reset() {
            this.position.set(0.0f);
            this.rotation = 0.0f;
        }
    }

    public static final class AABBEntry implements Presentable {
        public final Vector3f position = new Vector3f();
        public final Vector2f offset = new Vector2f();
        public final Vector2f size = new Vector2f();
        public float rotation;

        @Override
        public void reset() {
            this.position.set(0.0f);
            this.rotation = 0.0f;

            this.offset.set(0.0f);
            this.size.set(0.0f);
        }
    }
}
