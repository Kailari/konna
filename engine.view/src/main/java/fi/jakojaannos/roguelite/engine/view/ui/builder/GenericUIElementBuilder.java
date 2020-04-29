package fi.jakojaannos.roguelite.engine.view.ui.builder;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public class GenericUIElementBuilder extends UIElementBuilder<GenericUIElementBuilder> {
    public GenericUIElementBuilder(
            final UserInterface userInterface,
            final UIElement element,
            final String name
    ) {
        super(userInterface, element, name);
    }
}
