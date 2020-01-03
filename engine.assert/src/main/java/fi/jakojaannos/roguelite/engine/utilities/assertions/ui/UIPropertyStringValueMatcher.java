package fi.jakojaannos.roguelite.engine.utilities.assertions.ui;

import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UIPropertyStringValueMatcher<TParent extends UIElementMatcher>
        extends UIPropertyValueMatcher<String, TParent> {

    UIPropertyStringValueMatcher(
            final UIProperty<String> property,
            final String propertyValue,
            final TParent parent
    ) {
        super(property, propertyValue, parent);
    }

    public TParent whichContains(final String... strings) {
        assertAll(Arrays.stream(strings)
                        .map(string -> () -> assertTrue(this.propertyValue.contains(string))));
        return this.parent;
    }
}
