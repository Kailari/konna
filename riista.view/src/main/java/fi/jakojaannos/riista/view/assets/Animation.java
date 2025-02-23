package fi.jakojaannos.riista.view.assets;

import org.joml.Matrix4f;

import java.util.*;

public record Animation(
        String name,
        List<Frame>frames,
        double duration
) {
    public static record Frame(Matrix4f[]boneTransforms) {}
}
