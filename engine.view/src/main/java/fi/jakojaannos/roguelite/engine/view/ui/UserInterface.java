package fi.jakojaannos.roguelite.engine.view.ui;

import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIBuilder;

public interface UserInterface {
    static UIBuilder builder(
            final Viewport viewport,
            final SpriteBatch spriteBatch,
            final TextRenderer textRenderer
    ) {
        return new UIBuilder(viewport, spriteBatch, textRenderer);
    }

    void render();
}
