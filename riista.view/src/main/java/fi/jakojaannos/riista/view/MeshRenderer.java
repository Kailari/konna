package fi.jakojaannos.riista.view;

import fi.jakojaannos.riista.view.assets.SkeletalMesh;
import fi.jakojaannos.riista.data.components.Transform;

public interface MeshRenderer {
    void drawSkeletal(Transform transform, SkeletalMesh mesh, String animation, int frame);
}
