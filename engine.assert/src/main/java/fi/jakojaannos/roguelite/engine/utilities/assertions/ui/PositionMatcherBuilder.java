package fi.jakojaannos.roguelite.engine.utilities.assertions.ui;

import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.ui.query.UIPropertyMatcher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PositionMatcherBuilder {
    public static PositionMatcherBuilder isHorizontallyIn(final UserInterface ui) {
        return new PositionMatcherBuilder(ui.getWidth(), UIProperty.MIN_X, UIProperty.WIDTH);
    }

    public static PositionMatcherBuilder isVerticallyIn(final UserInterface ui) {
        return new PositionMatcherBuilder(ui.getHeight(), UIProperty.MIN_Y, UIProperty.HEIGHT);
    }

    private final int uiSize;
    private final UIProperty<Integer> minProperty;
    private final UIProperty<Integer> sizeProperty;

    public UIPropertyMatcher<Integer> min() {
        return uiElement -> {
            val minY = uiElement.getProperty(this.minProperty).orElseThrow();
            val height = uiElement.getProperty(this.sizeProperty).orElseThrow();

            val sectionMinY = 0;
            val sectionMaxY = this.uiSize * (1.0 / 3.0);
            val elementMiddleY = minY + height / 2.0;
            return elementMiddleY >= sectionMinY && elementMiddleY <= sectionMaxY;
        };
    }

    public UIPropertyMatcher<Integer> middle() {
        return uiElement -> {
            val minY = uiElement.getProperty(this.minProperty).orElseThrow();
            val height = uiElement.getProperty(this.sizeProperty).orElseThrow();

            val sectionMinY = this.uiSize * (1.0 / 3.0);
            val sectionMaxY = this.uiSize * (2.0 / 3.0);
            val elementMiddleY = minY + height / 2.0;
            return elementMiddleY >= sectionMinY && elementMiddleY <= sectionMaxY;
        };
    }

    public UIPropertyMatcher<Integer> max() {
        return uiElement -> {
            val minY = uiElement.getProperty(this.minProperty).orElseThrow();
            val height = uiElement.getProperty(this.sizeProperty).orElseThrow();

            val sectionMinY = this.uiSize * (2.0 / 3.0);
            val sectionMaxY = this.uiSize;
            val elementMiddleY = minY + height / 2.0;
            return elementMiddleY >= sectionMinY && elementMiddleY <= sectionMaxY;
        };
    }
}
