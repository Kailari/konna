package fi.jakojaannos.roguelite.assets;

import org.joml.Matrix4f;

public record MeshBone(
        int id,
        String name,
        Matrix4f transform
) {}
