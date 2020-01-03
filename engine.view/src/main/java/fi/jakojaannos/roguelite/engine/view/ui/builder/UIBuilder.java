package fi.jakojaannos.roguelite.engine.view.ui.builder;

import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.ui.internal.UserInterfaceImpl;

import java.util.function.Consumer;

public class UIBuilder {
    private final UserInterfaceImpl userInterface;

    public UIBuilder(
            final Viewport viewport,
            final TextSizeProvider fontSizeProvider
    ) {
        this.userInterface = new UserInterfaceImpl(viewport, fontSizeProvider);
    }

    public <T extends UIElementType<TBuilder>, TBuilder extends UIElementBuilder<TBuilder>> UIBuilder element(
            final String name,
            final T elementType,
            final Consumer<TBuilder> factory
    ) {
        this.userInterface.addElement(name, elementType, factory);
        return this;
    }

    public UserInterface build() {
        this.userInterface.getEntityManager().applyModifications();
        this.userInterface.updateHierarchy();
        return this.userInterface;
    }
}
