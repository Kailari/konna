package fi.jakojaannos.riista.vulkan.renderer.game;

import fi.jakojaannos.riista.view.DebugRenderer;
import fi.jakojaannos.riista.view.MeshRenderer;
import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.riista.view.ui.UiRenderer;
import fi.jakojaannos.riista.vulkan.application.PresentableState;
import fi.jakojaannos.riista.vulkan.renderer.debug.DebugRendererRecorder;
import fi.jakojaannos.riista.vulkan.renderer.mesh.MeshRendererRecorder;
import fi.jakojaannos.riista.vulkan.renderer.ui.UiRendererRecorder;

public record RendererRecorder(
        DebugRenderer debug,
        MeshRenderer mesh,
        UiRenderer ui
) implements Renderer {
    public void setWriteState(final PresentableState state) {
        ((DebugRendererRecorder) this.debug).setWriteState(state);
        ((MeshRendererRecorder) this.mesh).setWriteState(state);
        ((UiRendererRecorder) this.ui).setWriteState(state);
    }

    public RendererRecorder() {
        this(new DebugRendererRecorder(), new MeshRendererRecorder(), new UiRendererRecorder());
    }
}
