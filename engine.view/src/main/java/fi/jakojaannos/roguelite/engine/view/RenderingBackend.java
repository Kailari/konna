package fi.jakojaannos.roguelite.engine.view;

import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;

import java.nio.file.Path;

public interface RenderingBackend {
    Viewport createViewport(Window window);

    Camera getCamera(Viewport viewport);

    TextRenderer getTextRenderer(Path assetRoot, Camera camera);
}
