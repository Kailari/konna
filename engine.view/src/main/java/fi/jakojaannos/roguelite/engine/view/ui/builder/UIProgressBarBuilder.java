package fi.jakojaannos.roguelite.engine.view.ui.builder;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public class UIProgressBarBuilder extends UIElementBuilder<UIProgressBarBuilder> {
    public UIProgressBarBuilder(
            final UserInterface userInterface,
            final UIElement element,
            final String name
    ) {
        super(userInterface, element, name);
    }
}
