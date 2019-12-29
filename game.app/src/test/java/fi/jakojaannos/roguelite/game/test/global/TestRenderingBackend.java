package fi.jakojaannos.roguelite.game.test.global;

import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;

public class TestRenderingBackend {
    public static final UserInterface.ViewportSizeProvider VIEWPORT_SIZE_PROVIDER = new Viewport();
    public static final TextSizeProvider TEXT_SIZE_PROVIDER = new TextSize();

    private static class Viewport implements UserInterface.ViewportSizeProvider {
        @Override
        public int getWidthInPixels() {
            return 800;
        }

        @Override
        public int getHeightInPixels() {
            return 600;
        }
    }

    private static class TextSize implements TextSizeProvider {
        @Override
        public double getStringWidthInPixels(final int fontSize, final String text) {
            return fontSize / 2.0 * text.length();
        }
    }
}
