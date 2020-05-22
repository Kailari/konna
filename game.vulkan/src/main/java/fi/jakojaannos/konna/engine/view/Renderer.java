package fi.jakojaannos.konna.engine.view;

public interface Renderer {
    DebugRenderer debug();

    MeshRenderer mesh();

    UiRenderer ui();
}
