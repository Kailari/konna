package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering;

import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.lwjgl.system.MemoryStack;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;

// TODO: Get rid of the BufferedImage to get rid of the java.desktop module read
//  stb provides some image loading functionality, use that

@Slf4j
@EqualsAndHashCode
public class LWJGLTexture implements Texture, AutoCloseable {
    private final int texture;
    @Getter private final int width;
    @Getter private final int height;

    public LWJGLTexture(
            final int width,
            final int height,
            final BufferedImage image
    ) {
        this.texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.texture);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);


        try (val stack = MemoryStack.stackPush()) {
            val buffer = loadImageData(stack, image, width, height);
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
            MemoryStack stack,
            BufferedImage image,
            int width,
            int height
    ) {
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        val buffer = stack.malloc(width * height * 4);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
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
