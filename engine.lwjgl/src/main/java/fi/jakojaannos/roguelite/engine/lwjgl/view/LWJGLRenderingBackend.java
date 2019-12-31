package fi.jakojaannos.roguelite.engine.lwjgl.view;

import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.LWJGLTextRenderer;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.Window;
import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;

import java.nio.file.Path;

public class LWJGLRenderingBackend implements RenderingBackend {
    @Override
    public Camera getCamera(final Viewport viewport) {
        return new LWJGLCamera(viewport);
    }

    @Override
    public Viewport createViewport(final Window window) {
        return new LWJGLViewport(window.getWidth(), window.getHeight());
    }

    @Override
    public TextRenderer getTextRenderer(final Path assetRoot, final Camera camera) {
        return new LWJGLTextRenderer(assetRoot, camera);
    }
}
