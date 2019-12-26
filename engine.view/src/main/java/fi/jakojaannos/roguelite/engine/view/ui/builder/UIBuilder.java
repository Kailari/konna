package fi.jakojaannos.roguelite.engine.view.ui.builder;

import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementBuilder;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import lombok.val;

import java.util.function.Consumer;

public class UIBuilder {
    private final UserInterfaceImpl userInterface;

    public UIBuilder(
            final Viewport viewport,
            final SpriteBatch spriteBatch
    ) {
        this.userInterface = new UserInterfaceImpl(viewport, spriteBatch);
    }

    public <T extends UIElementType<TBuilder>, TBuilder extends UIElementBuilder<TBuilder>> UIBuilder element(
            final String name,
            final T elementType,
            final Consumer<TBuilder> factory
    ) {
        val elementEntity = this.userInterface.getEntityManager().createEntity();
        val builder = elementType.getBuilder(name, component -> this.userInterface.getEntityManager().addComponentTo(elementEntity, component));
        factory.accept(builder);
        return this;
    }

    public UserInterface build() {
        this.userInterface.getEntityManager().applyModifications();
        return this.userInterface;
    }
}
