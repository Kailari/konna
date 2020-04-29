package fi.jakojaannos.roguelite.engine.view.ui.builder;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public final class UIPanelBuilder extends UIElementBuilder<UIPanelBuilder> {
    public UIPanelBuilder(
            final UserInterface userInterface,
            final UIElement element,
            final String name
    ) {
        super(userInterface, element, name);
    }

    public UIPanelBuilder borderSize(final int value) {
        return property(UIProperty.BORDER_SIZE, value);
    }

    public UIPanelBuilder sprite(final String sprite) {
        return property(UIProperty.SPRITE, sprite);
    }
}
