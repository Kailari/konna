package fi.jakojaannos.konna.engine.application;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import fi.jakojaannos.konna.engine.view.renderer.RenderBuffer;
import fi.jakojaannos.konna.engine.view.renderer.debug.DebugRendererRecorder;
import fi.jakojaannos.konna.engine.view.renderer.mesh.MeshRendererRecorder;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

public class PresentableState {
    private final RenderBuffer<DebugRendererRecorder.TransformEntry> transformEntries = new RenderBuffer<>(DebugRendererRecorder.TransformEntry::new,
                                                                                                           DebugRendererRecorder.TransformEntry[]::new);
    private final RenderBuffer<MeshRendererRecorder.SkeletalEntry> skeletalMeshEntries = new RenderBuffer<>(MeshRendererRecorder.SkeletalEntry::new,
                                                                                                            MeshRendererRecorder.SkeletalEntry[]::new);
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Vector3f eyePosition = new Vector3f();

    private long timestamp;

    public RenderBuffer<DebugRendererRecorder.TransformEntry> transforms() {
        return this.transformEntries;
    }

    public RenderBuffer<MeshRendererRecorder.SkeletalEntry> skeletalMeshEntries() {
        return this.skeletalMeshEntries;
    }

    public long timestamp() {
        return this.timestamp;
    }

    public void clear(
            final TimeManager timeManager,
            final Vector3f eyePosition,
            final Matrix4f viewMatrix
    ) {
        this.transformEntries.reset();
        this.skeletalMeshEntries.reset();

        this.timestamp = timeManager.getCurrentGameTime();
        this.viewMatrix.set(viewMatrix);
        this.eyePosition.set(eyePosition);
    }

    public Matrix4f viewMatrix() {
        return this.viewMatrix;
    }

    public Vector3f eyePosition() {
        return this.eyePosition;
    }
}
