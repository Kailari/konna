package fi.jakojaannos.roguelite.engine.ui.builder;

import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.ui.internal.UserInterfaceImpl;
import lombok.val;

import java.util.function.Consumer;

public class UIBuilder {
    private final UserInterfaceImpl userInterface;

    public UIBuilder(
            final UserInterface.ViewportSizeProvider viewportSizeProvider,
            final TextSizeProvider fontSizeProvider
    ) {
        this.userInterface = new UserInterfaceImpl(viewportSizeProvider, fontSizeProvider);
    }

    public <T extends UIElementType<TBuilder>, TBuilder extends UIElementBuilder<TBuilder>> UIBuilder element(
            final String name,
            final T elementType,
            final Consumer<TBuilder> factory
    ) {
        val elementEntity = this.userInterface.getEntityManager().createEntity();
        val builder = elementType.getBuilder(this,
                                             elementEntity,
                                             name,
                                             component -> this.userInterface.getEntityManager().addComponentTo(elementEntity, component));
        factory.accept(builder);
        return this;
    }

    public UserInterface build() {
        this.userInterface.getEntityManager().applyModifications();
        return this.userInterface;
    }
}
