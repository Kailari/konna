package fi.jakojaannos.roguelite.engine.ui.builder;

import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.ui.internal.UserInterfaceImpl;
import org.joml.Vector2d;

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
        this.userInterface.addElement(name, elementType, factory);
        return this;
    }

    public UserInterface build() {
        this.userInterface.getEntityManager().applyModifications();
        this.userInterface.update(new Vector2d(-999.0), false);
        return this.userInterface;
    }
}
