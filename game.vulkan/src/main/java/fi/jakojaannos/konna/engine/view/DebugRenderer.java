package fi.jakojaannos.konna.engine.view;

import org.joml.Vector2f;

import fi.jakojaannos.roguelite.engine.data.components.Transform;

public interface DebugRenderer {
    void drawTransform(Transform transform);

    void drawBox(Transform transform, Vector2f offset, Vector2f size);
}
