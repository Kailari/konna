package fi.jakojaannos.roguelite.engine.ui;

import fi.jakojaannos.roguelite.engine.ui.builder.UIBuilder;
import org.joml.Vector2d;

import java.util.Queue;
import java.util.stream.Stream;

public interface UserInterface {
    static UIBuilder builder(
            final ViewportSizeProvider viewportSizeProvider,
            final TextSizeProvider fontSizeProvider
    ) {
        return new UIBuilder(viewportSizeProvider, fontSizeProvider);
    }

    Queue<UIEvent> update(Vector2d mousePos, boolean mouseClicked);

    Stream<UIElement> getRoots();

    interface ViewportSizeProvider {
        int getWidthInPixels();

        int getHeightInPixels();
    }
}
