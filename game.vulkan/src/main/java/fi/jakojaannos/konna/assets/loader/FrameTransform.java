package fi.jakojaannos.konna.assets.loader;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record FrameTransform(
        Vector3f translation,
        Quaternionf rotation,
        Vector3f scaling,
        Matrix4f matrix
) {}
