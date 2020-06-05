package fi.jakojaannos.konna.engine.view;

import fi.jakojaannos.konna.engine.view.ui.UiRenderer;

public interface Renderer {
    DebugRenderer debug();

    MeshRenderer mesh();

    UiRenderer ui();
}
