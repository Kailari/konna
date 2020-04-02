package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering;

import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import fi.jakojaannos.roguelite.engine.view.rendering.Texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

// TODO: Get rid of the BufferedImage to get rid of the java.desktop module read
//  stb provides some image loading functionality, use that

public class LWJGLTexture implements Texture {
    private static final Logger LOG = LoggerFactory.getLogger(LWJGLTexture.class);

    private final int texture;
    private final int width;
    private final int height;

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    public LWJGLTexture(
            final int width,
            final int height,
            final BufferedImage image
    ) {
        this.texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.texture);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        try (final var stack = MemoryStack.stackPush()) {
            final var buffer = loadImageData(stack, image, width, height);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        }

        this.width = width;
        this.height = height;
        LOG.debug("Done loading texture! {}×{}", width, height);
    }

    @Override
    public void use() {
        glBindTexture(GL_TEXTURE_2D, this.texture);
    }

    @Override
    public void close() {
        glDeleteTextures(this.texture);
    }

    private ByteBuffer loadImageData(
            final MemoryStack stack,
            final BufferedImage image,
            final int width,
            final int height
    ) {
        final int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        final var buffer = stack.malloc(width * height * 4);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int pixel = pixels[y * width + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));  // R
                buffer.put((byte) ((pixel >> 8) & 0xFF));   // G
                buffer.put((byte) (pixel & 0xFF));          // B
                buffer.put((byte) ((pixel >> 24) & 0xFF));  // A
            }
        }

        buffer.flip();

        return buffer;
    }
}
