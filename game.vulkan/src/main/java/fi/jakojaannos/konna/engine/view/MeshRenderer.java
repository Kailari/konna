package fi.jakojaannos.konna.engine.view;

import fi.jakojaannos.konna.engine.assets.SkeletalMesh;
import fi.jakojaannos.roguelite.engine.data.components.Transform;

public interface MeshRenderer {
    void drawSkeletal(Transform transform, SkeletalMesh mesh, String animation, int frame);
}
