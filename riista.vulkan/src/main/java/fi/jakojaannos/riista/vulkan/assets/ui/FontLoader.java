package fi.jakojaannos.riista.vulkan.assets.ui;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import fi.jakojaannos.riista.assets.AssetLoader;
import fi.jakojaannos.riista.view.assets.Font;
import fi.jakojaannos.riista.vulkan.internal.RenderingBackend;
import fi.jakojaannos.riista.vulkan.internal.device.DeviceContext;
import fi.jakojaannos.riista.vulkan.internal.window.Window;

import static org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;
import static org.lwjgl.system.MemoryStack.stackPush;

public class FontLoader implements AssetLoader<Font> {
    private static final Logger LOG = LoggerFactory.getLogger(FontLoader.class);

    private final DeviceContext deviceContext;

    private final float contentScaleX;
    private final float contentScaleY;

    public FontLoader(final RenderingBackend backend, final Window window) {
        this.deviceContext = backend.deviceContext();

        try (final var stack = stackPush()) {
            final var pX = stack.mallocFloat(1);
            final var pY = stack.mallocFloat(1);
            GLFW.glfwGetWindowContentScale(window.getHandle(), pX, pY);
            this.contentScaleX = pX.get(0);
            this.contentScaleY = pY.get(0);
        }
    }

    @Override
    public Optional<Font> load(final Path path) {
        final ByteBuffer rawTTF;
        try (final var fc = Files.newByteChannel(path)) {
            rawTTF = BufferUtils.createByteBuffer((int) fc.size() + 1);
            //noinspection StatementWithEmptyBody
            while (fc.read(rawTTF) != -1) ;
            rawTTF.flip();
        } catch (final IOException e) {
            LOG.error("Could not load font from {}!", path.toString());
            return Optional.empty();
        }

        final var fontInfo = STBTTFontinfo.create();
        if (!stbtt_InitFont(fontInfo, rawTTF)) {
            LOG.error("Failed to initialize font descriptor.");
            return Optional.empty();
        }

        final int ascent;
        final int descent;
        final int lineGap;
        try (final var stack = MemoryStack.stackPush()) {
            final var pAscent = stack.mallocInt(1);
            final var pDescent = stack.mallocInt(1);
            final var pLineGap = stack.mallocInt(1);

            stbtt_GetFontVMetrics(fontInfo, pAscent, pDescent, pLineGap);
            ascent = pAscent.get(0);
            descent = pDescent.get(0);
            lineGap = pLineGap.get(0);
        }

        return Optional.of(new FontImpl(this.deviceContext,
                                        rawTTF,
                                        fontInfo,
                                        ascent,
                                        descent,
                                        lineGap,
                                        this.contentScaleX,
                                        this.contentScaleY));
    }
}
