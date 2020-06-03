package fi.jakojaannos.konna.engine.application;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import fi.jakojaannos.konna.engine.view.renderer.RenderBuffer;
import fi.jakojaannos.konna.engine.view.renderer.debug.DebugRendererRecorder;
import fi.jakojaannos.konna.engine.view.renderer.mesh.MeshRendererRecorder;
import fi.jakojaannos.konna.engine.view.renderer.ui.UiRendererRecorder;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

public class PresentableState {
    private final RenderBuffer<DebugRendererRecorder.TransformEntry> transformEntries = new RenderBuffer<>(DebugRendererRecorder.TransformEntry::new,
                                                                                                           DebugRendererRecorder.TransformEntry[]::new);
    private final RenderBuffer<DebugRendererRecorder.AABBEntry> aabbEntries = new RenderBuffer<>(DebugRendererRecorder.AABBEntry::new,
                                                                                                 DebugRendererRecorder.AABBEntry[]::new);
    private final RenderBuffer<MeshRendererRecorder.SkeletalEntry> skeletalMeshEntries = new RenderBuffer<>(MeshRendererRecorder.SkeletalEntry::new,
                                                                                                            MeshRendererRecorder.SkeletalEntry[]::new);
    private final RenderBuffer<UiRendererRecorder.QuadEntry> quadEntries = new RenderBuffer<>(UiRendererRecorder.QuadEntry::new,
                                                                                              UiRendererRecorder.QuadEntry[]::new);
    private final RenderBuffer<UiRendererRecorder.TextEntry> textEntries = new RenderBuffer<>(UiRendererRecorder.TextEntry::new,
                                                                                              UiRendererRecorder.TextEntry[]::new);

    private final Matrix4f viewMatrix = new Matrix4f();
    private final Vector3f eyePosition = new Vector3f();
    private final UiVariables uiVariables = new UiVariables();

    private int gameModeId;
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
            final int gameModeId,
            final TimeManager timeManager,
            final Vector3f eyePosition,
            final Matrix4f viewMatrix
    ) {
        this.gameModeId = gameModeId;
        this.transformEntries.reset();
        this.skeletalMeshEntries.reset();
        this.quadEntries.reset();
        this.textEntries.reset();
        this.aabbEntries.reset();

        this.uiVariables.clear();

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

    public int gameModeId() {
        return this.gameModeId;
    }

    public RenderBuffer<UiRendererRecorder.QuadEntry> quadEntries() {
        return this.quadEntries;
    }

    public RenderBuffer<UiRendererRecorder.TextEntry> textEntries() {
        return this.textEntries;
    }

    public RenderBuffer<DebugRendererRecorder.AABBEntry> boxes() {
        return this.aabbEntries;
    }

    public UiVariables uiVariables() {
        return this.uiVariables;
    }
}
