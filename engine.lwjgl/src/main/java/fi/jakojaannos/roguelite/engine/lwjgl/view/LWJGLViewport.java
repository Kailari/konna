package fi.jakojaannos.roguelite.engine.lwjgl.view;

import fi.jakojaannos.roguelite.engine.view.Viewport;
import lombok.AllArgsConstructor;

import static org.lwjgl.opengl.GL11.glViewport;

@AllArgsConstructor
public class LWJGLViewport implements Viewport {
    private int width;
    private int height;

    @Override
    public int getWidthInPixels() {
        return this.width;
    }

    @Override
    public int getHeightInPixels() {
        return this.height;
    }

    @Override
    public void resize(final int width, final int height) {
        this.width = width;
        this.height = height;
        glViewport(0, 0, this.width, this.height);
    }
}
