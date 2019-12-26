package fi.jakojaannos.roguelite.engine.view.ui;

import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIBuilder;

public interface UserInterface<TTexture extends Texture> {
    static <TTexture extends Texture> UIBuilder<TTexture> builder(
            final Viewport viewport,
            final SpriteBatch<TTexture> spriteBatch,
            final SpriteRegistry<TTexture> spriteRegistry
    ) {
        return new UIBuilder<TTexture>(viewport, spriteBatch, spriteRegistry);
    }

    void render();
}
