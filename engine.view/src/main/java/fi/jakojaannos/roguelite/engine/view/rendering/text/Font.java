package fi.jakojaannos.roguelite.engine.view.rendering.text;

import java.nio.IntBuffer;

import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;

public interface Font extends TextSizeProvider, AutoCloseable {
    static int getCP(
            final String string,
            final int to,
            final int i,
            final IntBuffer outCodePoint
    ) {
        final var charA = string.charAt(i);
        if (Character.isHighSurrogate(charA) && i + 1 < to) {
            final var charB = string.charAt(i + 1);
            if (Character.isLowSurrogate(charB)) {
                outCodePoint.put(0, Character.toCodePoint(charA, charB));
                return 2;
            }
        }

        outCodePoint.put(0, charA);
        return 1;
    }

    FontTexture getTextureForSize(int fontSize);

    float getLineOffset();

    default boolean isKerningEnabled() {
        return false;
    }
}
