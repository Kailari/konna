package fi.jakojaannos.roguelite.engine.ui;

import fi.jakojaannos.roguelite.engine.ui.builder.UIBuilder;
import org.joml.Vector2d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class UserInterfaceTest {
    private static final int VIEWPORT_WIDTH = 800;
    private static final int VIEWPORT_HEIGHT = 600;

    private UIBuilder uiBuilder;

    @BeforeEach
    void beforeEach() {
        uiBuilder = UserInterface.builder(new UserInterface.ViewportSizeProvider() {
            @Override
            public int getWidthInPixels() {
                return VIEWPORT_WIDTH;
            }

            @Override
            public int getHeightInPixels() {
                return VIEWPORT_HEIGHT;
            }
        }, (fontSize, text) -> fontSize / 1.5 * text.length());
    }

    @Test
    void buildingUIElementWithoutDefiningBoundsDefaultsToFill() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> { })
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_X).equalTo(0)
                .with(UIProperty.MAX_X).equalTo(VIEWPORT_WIDTH)
                .with(UIProperty.MIN_Y).equalTo(0)
                .with(UIProperty.MAX_Y).equalTo(VIEWPORT_HEIGHT);
    }

    private static AssertUI assertUI(final UserInterface userInterface) {
        return new AssertUI(userInterface);
    }

    private static class AssertUI {
        private final UserInterface userInterface;

        public AssertUI(final UserInterface userInterface) {
            this.userInterface = userInterface;
        }

        public UIElementMatcher hasExactlyOneElementWithName(final String name) {
            List<UIElement> elements = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, value -> value.equals(name))
                                                    .collect(Collectors.toList());

            assertEquals(1, elements.size(), "Expected there to be exactly one element with name \"" + name + "\"");
            return new UIElementMatcher(elements.get(0));
        }
    }

    private static class UIElementMatcher {
        private final UIElement uiElement;

        public UIElementMatcher(final UIElement uiElement) {
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

    private static class UIPropertyValueMatcher<T> {
        private final UIProperty<T> property;
        private final T propertyValue;
        private final UIElementMatcher parent;

        public UIPropertyValueMatcher(
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
}
