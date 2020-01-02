package fi.jakojaannos.roguelite.engine.lwjgl.view;

import fi.jakojaannos.roguelite.engine.view.Viewport;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.opengl.GL11.glViewport;

@Slf4j
public class LWJGLViewport extends Viewport {
    public LWJGLViewport(final int width, final int height) {
        super(width, height);
    }

    @Override
    public void resize(final int width, final int height) {
        super.resize(width, height);
        glViewport(0, 0, width, height);
        LOG.info("Resizing viewport: {}x{}", width, height);
    }
}
