package fi.jakojaannos.roguelite.engine.view.test.utilities.ui;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UIElementMatcher {
    private final UIElement uiElement;

    UIElementMatcher(final UIElement uiElement) {
        this.uiElement = uiElement;
    }

    public <T> UIElementMatcher without(final UIProperty<T> property) {
        Optional<T> propertyValue = this.uiElement.getProperty(property);
        assertFalse(propertyValue.isPresent(),
                    String.format("Expected ui element \"%s\" to have the property \"%s\"",
                                  this.uiElement.getProperty(UIProperty.NAME).orElse("unnamed"),
                                  property.getName()));
        return this;
    }

    public <T> UIPropertyValueMatcher<T> with(final UIProperty<T> property) {
        Optional<T> propertyValue = this.uiElement.getProperty(property);
        assertTrue(propertyValue.isPresent(),
                   String.format("Expected ui element \"%s\" to have the property \"%s\"",
                                 this.uiElement.getProperty(UIProperty.NAME).orElse("unnamed"),
                                 property.getName()));
        return new UIPropertyValueMatcher<>(property, propertyValue.get(), this);
    }
}
