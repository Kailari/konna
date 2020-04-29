package fi.jakojaannos.roguelite.engine.view.ui.builder;

import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.ui.UIBuilder;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementBuilder;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.ui.internal.UserInterfaceImpl;

public class UIBuilderImpl implements UIBuilder {
    private final UserInterfaceImpl userInterface;

    public UIBuilderImpl(
            final Events events,
            final TimeManager timeManager,
            final Viewport viewport,
            final TextSizeProvider fontSizeProvider
    ) {
        this.userInterface = new UserInterfaceImpl(events, timeManager, viewport, fontSizeProvider);
    }

    @Override
    public UIBuilder element(
            final String name,
            final Consumer<UIElementBuilder> factory
    ) {
        this.userInterface.addElement(name, factory);
        return this;
    }

    @Override
    public UserInterface build() {
        this.userInterface.update(new Mouse());
        return this.userInterface;
    }
}
