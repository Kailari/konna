package fi.jakojaannos.roguelite.engine.view.ui.builder;

import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementBuilder;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import lombok.val;

import java.util.function.Consumer;

public class UIBuilder<TTexture extends Texture> {
    private final UserInterfaceImpl<TTexture> userInterface;

    public UIBuilder(
            final Viewport viewport,
            final SpriteBatch<TTexture> spriteBatch,
            final SpriteRegistry<TTexture> spriteRegistry
    ) {
        this.userInterface = new UserInterfaceImpl<>(viewport, spriteBatch, spriteRegistry);
    }

    public <T extends UIElementType<TBuilder>, TBuilder extends UIElementBuilder<TBuilder>> UIBuilder<TTexture> element(
            final String name,
            final T elementType,
            final Consumer<TBuilder> factory
    ) {
        val elementEntity = this.userInterface.getEntityManager().createEntity();
        val builder = elementType.getBuilder(name, component -> this.userInterface.getEntityManager().addComponentTo(elementEntity, component));
        factory.accept(builder);
        return this;
    }

    public UserInterface<TTexture> build() {
        this.userInterface.getEntityManager().applyModifications();
        return this.userInterface;
    }
}
