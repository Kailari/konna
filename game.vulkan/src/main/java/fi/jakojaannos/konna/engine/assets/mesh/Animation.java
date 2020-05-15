package fi.jakojaannos.konna.engine.assets.mesh;

import org.joml.Matrix4f;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import fi.jakojaannos.konna.engine.assets.loader.FrameTransform;

public record Animation(
        String name,
        List<Frame>frames,
        double duration
) {
    public static record Frame(Matrix4f[]boneTransforms) {}
}
