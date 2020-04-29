package fi.jakojaannos.roguelite.engine.view.ui.builder;

import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.ui.internal.UserInterfaceImpl;

public class UIBuilder {
    private final UserInterfaceImpl userInterface;

    public UIBuilder(
            final Events events,
            final TimeManager timeManager,
            final Viewport viewport,
            final TextSizeProvider fontSizeProvider
    ) {
        this.userInterface = new UserInterfaceImpl(events, timeManager, viewport, fontSizeProvider);
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
        this.userInterface.update(new Mouse());
        return this.userInterface;
    }
}
