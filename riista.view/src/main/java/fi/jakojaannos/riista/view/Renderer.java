package fi.jakojaannos.riista.view;

import fi.jakojaannos.riista.view.ui.UiRenderer;

public interface Renderer {
    DebugRenderer debug();

    MeshRenderer mesh();

    UiRenderer ui();
}
