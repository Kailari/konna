package fi.jakojaannos.konna.engine.assets.mesh;

import org.joml.Matrix4f;

import java.util.*;

public record Animation(
        String name,
        List<Frame>frames,
        double duration
) {
    public static record Frame(Matrix4f[]boneTransforms) {}
}
