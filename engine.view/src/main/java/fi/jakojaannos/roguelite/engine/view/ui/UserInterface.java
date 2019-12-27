package fi.jakojaannos.roguelite.engine.view.ui;

import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIBuilder;
import org.joml.Vector2d;

import java.util.Queue;

public interface UserInterface {
    static UIBuilder builder(
            final Viewport viewport,
            final SpriteBatch spriteBatch,
            final TextRenderer textRenderer
    ) {
        return new UIBuilder(viewport, spriteBatch, textRenderer);
    }

    void render();

    Queue<UIEvent> pollEvents(Vector2d mousePos, boolean mouseClicked);
}
