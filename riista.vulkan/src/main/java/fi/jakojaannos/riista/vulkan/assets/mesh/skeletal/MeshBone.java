package fi.jakojaannos.riista.vulkan.assets.mesh.skeletal;

import org.joml.Matrix4f;

public record MeshBone(
        int id,
        String name,
        Matrix4f transform
) {}
