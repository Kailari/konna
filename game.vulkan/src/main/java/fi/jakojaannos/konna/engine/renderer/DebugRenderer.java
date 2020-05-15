package fi.jakojaannos.konna.engine.renderer;

import org.joml.Vector3f;

import fi.jakojaannos.roguelite.engine.data.components.Transform;

public interface DebugRenderer {
    void drawTransform(Transform transform);

    final class TransformEntry implements Presentable {
        public Vector3f position = new Vector3f();
        public float rotation;

        @Override
        public void reset() {
            this.position.set(0.0f);
            this.rotation = 0.0f;
        }
    }
}
