package fi.jakojaannos.roguelite.engine.utilities.assertions.ui;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import lombok.val;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UILabelMatcher extends UIElementMatcher {
    UILabelMatcher(final UIElement uiElement, final int uiWidth, final int uiHeight) {
        super(uiElement, uiWidth, uiHeight);
    }

    public UIPropertyStringValueMatcher<UILabelMatcher> hasText() {
        val propertyValue = this.uiElement.getProperty(UIProperty.TEXT);
        assertTrue(propertyValue.isPresent(),
                   String.format("Expected ui element \"%s\" to have the property \"%s\"",
                                 getNameOrDefault(),
                                 UIProperty.TEXT.getName()));
        return new UIPropertyStringValueMatcher<>(UIProperty.TEXT, propertyValue.get(), this);
    }
}
