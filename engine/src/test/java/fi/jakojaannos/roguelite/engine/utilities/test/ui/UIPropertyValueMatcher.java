package fi.jakojaannos.roguelite.engine.utilities.test.ui;

import fi.jakojaannos.roguelite.engine.ui.UIProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UIPropertyValueMatcher<T> {
    private final UIProperty<T> property;
    private final T propertyValue;
    private final UIElementMatcher parent;

    UIPropertyValueMatcher(
            final UIProperty<T> property,
            final T propertyValue,
            final UIElementMatcher parent
    ) {
        this.property = property;
        this.propertyValue = propertyValue;
        this.parent = parent;
    }

    public UIElementMatcher equalTo(final T value) {
        assertEquals(value, this.propertyValue,
                     String.format("Expected property \"%s\" to have value \"%s\", but value was \"%s\"",
                                   this.property.getName(),
                                   value,
                                   this.propertyValue));
        return this.parent;
    }
}
