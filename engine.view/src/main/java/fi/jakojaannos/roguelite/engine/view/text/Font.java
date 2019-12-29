package fi.jakojaannos.roguelite.engine.view.text;

import lombok.val;

import java.nio.IntBuffer;

public interface Font {
    double getStringWidthInPixels(int fontSize, String string);

    FontTexture getTextureForSize(int fontSize);

    float getLineOffset();

    default boolean isKerningEnabled() {
        return false;
    }

    default double getStringHeightInPixels(int fontSize, String string) {
        return fontSize;
    }

    static int getCP(
            final String string,
            final int to,
            final int i,
            final IntBuffer outCodePoint
    ) {
        val charA = string.charAt(i);
        if (Character.isHighSurrogate(charA) && i + 1 < to) {
            val charB = string.charAt(i + 1);
            if (Character.isLowSurrogate(charB)) {
                outCodePoint.put(0, Character.toCodePoint(charA, charB));
                return 2;
            }
        }

        outCodePoint.put(0, charA);
        return 1;
    }
}
