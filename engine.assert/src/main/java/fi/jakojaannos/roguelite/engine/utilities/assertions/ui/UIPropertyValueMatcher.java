package fi.jakojaannos.roguelite.engine.utilities.assertions.ui;

import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UIPropertyValueMatcher<T, TParent extends UIElementMatcher> {
    private final UIProperty<T> property;
    protected final T propertyValue;
    protected final TParent parent;

    UIPropertyValueMatcher(
            final UIProperty<T> property,
            final T propertyValue,
            final TParent parent
    ) {
        this.property = property;
        this.propertyValue = propertyValue;
        this.parent = parent;
    }

    public TParent equalTo(final T value) {
        assertEquals(value, this.propertyValue,
                     String.format("Unexpected property \"%s\" value",
                                   this.property.getName()));
        return this.parent;
    }
}
