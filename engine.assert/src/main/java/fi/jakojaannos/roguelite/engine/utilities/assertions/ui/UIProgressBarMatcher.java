package fi.jakojaannos.roguelite.engine.utilities.assertions.ui;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;

public class UIProgressBarMatcher extends UIElementMatcher {
    UIProgressBarMatcher(
            final UIElement uiElement,
            final int uiWidth,
            final int uiHeight
    ) {
        super(uiElement, uiWidth, uiHeight);
    }
}
