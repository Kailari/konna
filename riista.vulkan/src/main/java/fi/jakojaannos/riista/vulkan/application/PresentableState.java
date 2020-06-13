package fi.jakojaannos.riista.vulkan.application;

import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector3f;
import org.lwjgl.vulkan.VkExtent2D;

import fi.jakojaannos.riista.vulkan.renderer.game.RenderBuffer;
import fi.jakojaannos.riista.vulkan.renderer.debug.DebugRendererRecorder;
import fi.jakojaannos.riista.vulkan.renderer.mesh.MeshRendererRecorder;
import fi.jakojaannos.riista.vulkan.renderer.ui.UiRendererRecorder;
import fi.jakojaannos.riista.utilities.TimeManager;

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

    private final Vector2d mousePosition = new Vector2d();
    private int framebufferWidth;
    private int framebufferHeight;

    private boolean mouseClicked;
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

    public int framebufferWidth() {
        return this.framebufferWidth;
    }

    public int framebufferHeight() {
        return this.framebufferHeight;
    }

    public Matrix4f viewMatrix() {
        return this.viewMatrix;
    }

    public Vector3f eyePosition() {
        return this.eyePosition;
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

    public Vector2d mousePosition() {
        return this.mousePosition;
    }

    public boolean mouseClicked() {
        return this.mouseClicked;
    }

    public void clear(
            final TimeManager timeManager,
            final Vector2d mousePosition,
            final boolean mouseClicked,
            final VkExtent2D swapchainExtent,
            final Vector3f eyePosition,
            final Matrix4f viewMatrix
    ) {
        this.mouseClicked = mouseClicked;
        this.transformEntries.reset();
        this.skeletalMeshEntries.reset();
        this.quadEntries.reset();
        this.textEntries.reset();
        this.aabbEntries.reset();

        this.uiVariables.clear();

        this.framebufferWidth = swapchainExtent.width();
        this.framebufferHeight = swapchainExtent.height();
        this.mousePosition.set(mousePosition);
        this.timestamp = timeManager.getCurrentGameTime();
        this.viewMatrix.set(viewMatrix);
        this.eyePosition.set(eyePosition);
    }
}
