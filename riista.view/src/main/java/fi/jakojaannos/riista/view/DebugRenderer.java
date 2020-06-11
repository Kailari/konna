package fi.jakojaannos.riista.view;

import org.joml.Vector2f;

import fi.jakojaannos.riista.data.components.Transform;

public interface DebugRenderer {
    void drawTransform(Transform transform);

    void drawBox(Transform transform, Vector2f offset, Vector2f size);
}
