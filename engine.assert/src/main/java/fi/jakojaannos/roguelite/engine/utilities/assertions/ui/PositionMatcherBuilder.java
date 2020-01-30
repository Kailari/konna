package fi.jakojaannos.roguelite.engine.utilities.assertions.ui;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.ui.query.UIPropertyMatcher;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PositionMatcherBuilder {
    private final int uiSize;
    private final UIProperty<Integer> minProperty;
    private final UIProperty<Integer> sizeProperty;

    public static PositionMatcherBuilder isHorizontallyIn(final UserInterface ui) {
        return new PositionMatcherBuilder(ui.getWidth(), UIProperty.MIN_X, UIProperty.WIDTH);
    }

    public static PositionMatcherBuilder isVerticallyIn(final UserInterface ui) {
        return new PositionMatcherBuilder(ui.getHeight(), UIProperty.MIN_Y, UIProperty.HEIGHT);
    }

    public UIPropertyMatcher<Integer> min() {
        return uiElement -> {
            final var minY = uiElement.getProperty(this.minProperty).orElseThrow();
            final var height = uiElement.getProperty(this.sizeProperty).orElseThrow();

            final var sectionMinY = 0;
            final var sectionMaxY = this.uiSize * (1.0 / 3.0);
            final var elementMiddleY = minY + height / 2.0;
            return elementMiddleY >= sectionMinY && elementMiddleY <= sectionMaxY;
        };
    }

    public UIPropertyMatcher<Integer> middle() {
        return uiElement -> {
            final var minY = uiElement.getProperty(this.minProperty).orElseThrow();
            final var height = uiElement.getProperty(this.sizeProperty).orElseThrow();

            final var sectionMinY = this.uiSize * (1.0 / 3.0);
            final var sectionMaxY = this.uiSize * (2.0 / 3.0);
            final var elementMiddleY = minY + height / 2.0;
            return elementMiddleY >= sectionMinY && elementMiddleY <= sectionMaxY;
        };
    }

    public UIPropertyMatcher<Integer> max() {
        return uiElement -> {
            final var minY = uiElement.getProperty(this.minProperty).orElseThrow();
            final var height = uiElement.getProperty(this.sizeProperty).orElseThrow();

            final var sectionMinY = this.uiSize * (2.0 / 3.0);
            final var sectionMaxY = this.uiSize;
            final var elementMiddleY = minY + height / 2.0;
            return elementMiddleY >= sectionMinY && elementMiddleY <= sectionMaxY;
        };
    }
}
