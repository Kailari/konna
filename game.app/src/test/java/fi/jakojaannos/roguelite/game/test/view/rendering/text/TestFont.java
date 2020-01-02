package fi.jakojaannos.roguelite.game.test.view.rendering.text;

import fi.jakojaannos.roguelite.engine.view.rendering.text.Font;
import fi.jakojaannos.roguelite.engine.view.rendering.text.FontTexture;

import java.nio.file.Path;

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
