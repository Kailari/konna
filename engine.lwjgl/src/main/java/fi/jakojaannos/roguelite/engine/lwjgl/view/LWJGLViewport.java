package fi.jakojaannos.roguelite.engine.lwjgl.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.jakojaannos.roguelite.engine.view.Viewport;

import static org.lwjgl.opengl.GL11.glViewport;

public class LWJGLViewport extends Viewport {
    private static final Logger LOG = LoggerFactory.getLogger(LWJGLViewport.class);

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
