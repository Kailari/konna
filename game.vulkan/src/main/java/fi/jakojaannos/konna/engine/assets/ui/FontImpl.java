package fi.jakojaannos.konna.engine.assets.ui;

import org.lwjgl.stb.STBTTFontinfo;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import fi.jakojaannos.konna.engine.assets.Font;
import fi.jakojaannos.konna.engine.assets.FontTexture;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;

public class FontImpl implements Font {
    private final DeviceContext deviceContext;

    private final ByteBuffer rawTTF;
    private final STBTTFontinfo fontInfo;
    private final int ascent;
    private final int descent;
    private final int lineGap;
    private final float contentScaleX;
    private final float contentScaleY;

    private final Map<Integer, FontTextureImpl> sizes = new HashMap<>();

    @Override
    public float getLineOffset() {
        return this.ascent - this.descent + this.lineGap;
    }

    @Override
    public boolean isKerningEnabled() {
        // FIXME: Allow kerning
        return false;
    }

    public FontImpl(
            final DeviceContext deviceContext,
            final ByteBuffer rawTTF,
            final STBTTFontinfo fontInfo,
            final int ascent,
            final int descent,
            final int lineGap,
            final float contentScaleX,
            final float contentScaleY
    ) {
        this.deviceContext = deviceContext;

        this.rawTTF = rawTTF;
        this.fontInfo = fontInfo;
        this.ascent = ascent;
        this.descent = descent;
        this.lineGap = lineGap;
        this.contentScaleX = contentScaleX;
        this.contentScaleY = contentScaleY;
    }

    @Override
    public FontTexture getForSize(final int fontSize) {
        return this.sizes.computeIfAbsent(fontSize,
                                          key -> new FontTextureImpl(this.deviceContext,
                                                                     this.rawTTF,
                                                                     this,
                                                                     this.fontInfo,
                                                                     key,
                                                                     this.contentScaleX,
                                                                     this.contentScaleY));
    }

    @Override
    public void close() {
        this.fontInfo.free();
        this.sizes.forEach((size, fontTexture) -> fontTexture.close());
    }
}
