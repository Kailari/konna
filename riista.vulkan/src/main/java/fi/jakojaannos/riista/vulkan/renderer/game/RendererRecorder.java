package fi.jakojaannos.riista.vulkan.renderer.game;

import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.riista.vulkan.application.PresentableState;
import fi.jakojaannos.riista.vulkan.renderer.debug.DebugRendererRecorder;
import fi.jakojaannos.riista.vulkan.renderer.mesh.MeshRendererRecorder;
import fi.jakojaannos.riista.vulkan.renderer.ui.UiRendererRecorder;

public record RendererRecorder(
        DebugRendererRecorder debug,
        MeshRendererRecorder mesh,
        UiRendererRecorder ui
) implements Renderer {
    public void setWriteState(final PresentableState state) {
        this.debug.setWriteState(state);
        this.mesh.setWriteState(state);
        this.ui.setWriteState(state);
    }

    public RendererRecorder() {
        this(new DebugRendererRecorder(), new MeshRendererRecorder(), new UiRendererRecorder());
    }
}
