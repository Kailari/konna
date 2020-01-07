package fi.jakojaannos.roguelite.engine.utilities.assertions.ui;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UIElementMatcher {
    protected final UIElement uiElement;
    protected final int uiHeight, uiWidth;

    protected String getNameOrDefault() {
        return this.uiElement.getProperty(UIProperty.NAME).orElse("unnamed");
    }

    UIElementMatcher(
            final UIElement uiElement,
            final int uiWidth,
            final int uiHeight
    ) {
        this.uiElement = uiElement;
        this.uiWidth = uiWidth;
        this.uiHeight = uiHeight;
    }

    public VerticalPositionMatcher<UIElementMatcher> isVerticallyIn() {
        int minY = this.uiElement.getProperty(UIProperty.MIN_Y).orElseThrow();
        int maxY = this.uiElement.getProperty(UIProperty.MAX_Y).orElseThrow();
        int height = this.uiElement.getProperty(UIProperty.HEIGHT).orElseThrow();
        return new VerticalPositionMatcher<>(this, getNameOrDefault(), minY, maxY, height, this.uiHeight);
    }

    public <T> UIElementMatcher without(final UIProperty<T> property) {
        Optional<T> propertyValue = this.uiElement.getProperty(property);
        assertFalse(propertyValue.isPresent(),
                    String.format("Expected ui element \"%s\" to have the property \"%s\"",
                                  getNameOrDefault(),
                                  property.getName()));
        return this;
    }

    public <T> UIPropertyValueMatcher<T, UIElementMatcher> with(final UIProperty<T> property) {
        return withProperty(property, this);
    }

    protected static <T, TParent extends UIElementMatcher> UIPropertyValueMatcher<T, TParent> withProperty(
            final UIProperty<T> property,
            final TParent parent
    ) {
        Optional<T> propertyValue = parent.uiElement.getProperty(property);
        assertTrue(propertyValue.isPresent(),
                   String.format("Expected ui element \"%s\" to have the property \"%s\"",
                                 parent.getNameOrDefault(),
                                 property.getName()));
        return new UIPropertyValueMatcher<>(property, propertyValue.get(), parent);
    }

    public UILabelMatcher isLabel() {
        assertTrue(this.uiElement.getProperty(UIProperty.TYPE)
                                 .filter(UIElementType.LABEL::equals)
                                 .isPresent());
        return new UILabelMatcher(this.uiElement, this.uiWidth, this.uiHeight);
    }

    public UIProgressBarMatcher isProgressBar() {
        return null;
    }

    public UIElementMatcher isHidden() {
        assertTrue(this.uiElement.getProperty(UIProperty.HIDDEN)
                                 .orElse(false));
        return this;
    }

    public UIElementMatcher isVisible() {
        assertFalse(this.uiElement.getProperty(UIProperty.HIDDEN)
                                  .orElse(false));
        return this;
    }

    public UIElementMatcher hasChildMatching(final Consumer<UIElementMatcher> matcher) {
        assertTrue(this.uiElement.getChildren().stream().anyMatch(child -> {
            try {
                matcher.accept(new UIElementMatcher(child, uiWidth, uiHeight));
                return true;
            } catch (AssertionError ignored) {
                return false;
            }
        }));

        return this;
    }

    public UIPropertyStringValueMatcher<UIElementMatcher> withName() {
        Optional<String> propertyValue = this.uiElement.getProperty(UIProperty.NAME);
        assertTrue(propertyValue.isPresent(), "Expected ui element to have a name");
        return new UIPropertyStringValueMatcher<>(UIProperty.NAME, propertyValue.get(), this);
    }

    public UIElement getElement() {
        return this.uiElement;
    }
}
