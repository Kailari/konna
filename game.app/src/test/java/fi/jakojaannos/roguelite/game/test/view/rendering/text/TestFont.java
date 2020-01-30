package fi.jakojaannos.roguelite.game.test.view.rendering.text;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import fi.jakojaannos.roguelite.engine.view.rendering.text.Font;
import fi.jakojaannos.roguelite.engine.view.rendering.text.FontTexture;
import fi.jakojaannos.roguelite.engine.view.rendering.text.RenderableCharacter;

import static org.mockito.Mockito.mock;

public class TestFont implements Font {
    public TestFont(final Path path, final float contentScaleX, final float contentScaleY) {
    }

    @Override
    public FontTexture getTextureForSize(final int fontSize) {
        return new FontTexture() {
            @Override
            public void use() {
            }

            @Override
            public float getPixelHeightScale() {
                return fontSize;
            }

            @Override
            public RenderableCharacter getNextCharacterAndAdvance(
                    final int codePoint,
                    final IntBuffer pCodePoint,
                    final FloatBuffer pX,
                    final FloatBuffer pY,
                    final int i,
                    final int to,
                    final String string,
                    final float factorX
            ) {
                return mock(RenderableCharacter.class);
            }

            @Override
            public float getContentScaleX() {
                return 1;
            }

            @Override
            public float getContentScaleY() {
                return 1;
            }
        };
    }

    @Override
    public float getLineOffset() {
        return 1.1f;
    }

    @Override
    public double getStringWidthInPixels(final int fontSize, final String text) {
        return (fontSize / 1.5) * text.length();
    }

    @Override
    public void close() {
    }
}
