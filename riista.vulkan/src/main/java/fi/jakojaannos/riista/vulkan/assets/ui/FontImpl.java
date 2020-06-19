package fi.jakojaannos.riista.vulkan.assets.ui;

import org.lwjgl.stb.STBTTFontinfo;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import fi.jakojaannos.riista.view.assets.Font;
import fi.jakojaannos.riista.view.assets.FontTexture;
import fi.jakojaannos.riista.vulkan.internal.device.DeviceContext;

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
        // FIXME: Allow configuring kerning
        return true;
    }

    public STBTTFontinfo getFontInfo() {
        return this.fontInfo;
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
        // FIXME: STBTT supports packing multiple font sizes within a single texture
        //  - instead of having individual font textures for each font size, pre-determine required
        //    sizes and generate textures based on that. This allows smarter batching in the text
        //    renderer, potentially allowing all glyphs for a given font to be rendered in a single
        //    batch.
        //  - Current approach: Generate a new font texture when a new size is requested
        //  - Naive approach: Re-generate font texture atlas with all font sizes when new size is requested
        //  - Improvements:
        //      A. Pre-determine what font sizes are needed and pre-generate texture atlas
        //      B. Do texture generation after all render commands are recorded to avoid generating
        //         multiple times for a single frame
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
