package fi.jakojaannos.roguelite.engine.utilities.assertions.ui;

import java.util.function.Function;

import fi.jakojaannos.roguelite.engine.view.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.ui.query.UIPropertyMatcher;

public class PositionMatcherBuilder {
    private final int uiSize;
    private final Function<ElementBoundaries, Integer> minGetter;
    private final Function<ElementBoundaries, Integer> sizeGetter;

    PositionMatcherBuilder(
            final int uiSize,
            final Function<ElementBoundaries, Integer> minGetter,
            final Function<ElementBoundaries, Integer> sizeGetter
    ) {
        this.uiSize = uiSize;
        this.minGetter = minGetter;
        this.sizeGetter = sizeGetter;
    }

    public static PositionMatcherBuilder isHorizontallyIn(final UserInterface ui) {
        return new PositionMatcherBuilder(ui.getWidth(), ElementBoundaries::getMinX, ElementBoundaries::getWidth);
    }

    public static PositionMatcherBuilder isVerticallyIn(final UserInterface ui) {
        return new PositionMatcherBuilder(ui.getHeight(), ElementBoundaries::getMinY, ElementBoundaries::getHeight);
    }

    public UIPropertyMatcher<Integer> min() {
        return uiElement -> {
            final var min = this.minGetter.apply(uiElement.getBounds());
            final var size = this.sizeGetter.apply(uiElement.getBounds());

            final var sectionMin = 0;
            final var sectionMax = this.uiSize * (1.0 / 3.0);
            final var elementMiddle = min + size / 2.0;
            return elementMiddle >= sectionMin && elementMiddle <= sectionMax;
        };
    }

    public UIPropertyMatcher<Integer> middle() {
        return uiElement -> {
            final var min = this.minGetter.apply(uiElement.getBounds());
            final var size = this.sizeGetter.apply(uiElement.getBounds());

            final var sectionMin = this.uiSize * (1.0 / 3.0);
            final var sectionMax = this.uiSize * (2.0 / 3.0);
            final var elementMiddle = min + size / 2.0;
            return elementMiddle >= sectionMin && elementMiddle <= sectionMax;
        };
    }

    public UIPropertyMatcher<Integer> max() {
        return uiElement -> {
            final var min = this.minGetter.apply(uiElement.getBounds());
            final var size = this.sizeGetter.apply(uiElement.getBounds());

            final var sectionMin = this.uiSize * (2.0 / 3.0);
            final var elementMiddle = min + size / 2.0;
            return elementMiddle >= sectionMin && elementMiddle <= this.uiSize;
        };
    }
}
