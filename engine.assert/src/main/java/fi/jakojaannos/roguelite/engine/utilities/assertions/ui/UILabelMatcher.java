package fi.jakojaannos.roguelite.engine.utilities.assertions.ui;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;

public class UILabelMatcher extends UIElementMatcher {
    UILabelMatcher(final UIElement uiElement, final int uiWidth, final int uiHeight) {
        super(uiElement, uiWidth, uiHeight);
    }

    public UIPropertyValueMatcher<String, UILabelMatcher> hasText() {
        return withProperty(UIProperty.TEXT, this);
    }
}
