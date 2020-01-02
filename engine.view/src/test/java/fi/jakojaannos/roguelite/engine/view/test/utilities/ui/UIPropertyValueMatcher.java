package fi.jakojaannos.roguelite.engine.view.test.utilities.ui;

import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;

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
                     String.format("Unexpected property \"%s\" value",
                                   this.property.getName()));
        return this.parent;
    }
}
