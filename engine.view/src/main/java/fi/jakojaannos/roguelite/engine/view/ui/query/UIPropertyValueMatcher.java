package fi.jakojaannos.roguelite.engine.view.ui.query;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;

/**
 * Utility for constructing simple matchers on UI property values
 *
 * @param <T>
 */
@RequiredArgsConstructor
public class UIPropertyValueMatcher<T> implements UIPropertyMatcher<T> {
    private final UIProperty<T> property;
    private final Predicate<Optional<T>> matcher;

    @Override
    public boolean evaluate(final UIElement uiElement) {
        return this.matcher.test(uiElement.getProperty(this.property));
    }

    @RequiredArgsConstructor
    public static class Builder<T> {
        private final UIProperty<T> property;

        public UIPropertyValueMatcher<T> isUndefinedOr(final T value) {
            return new UIPropertyValueMatcher<>(this.property,
                                                maybeActual -> maybeActual.map(actual -> actual.equals(value))
                                                                          .orElse(true));
        }

        public UIPropertyValueMatcher<T> ifPresentOrElse(
                final Predicate<T> ifPresent,
                final Supplier<Boolean> orElse
        ) {
            return new UIPropertyValueMatcher<>(this.property,
                                                maybeActual -> maybeActual.map(ifPresent::test)
                                                                          .orElseGet(orElse));
        }

        public UIPropertyValueMatcher<T> isPresentAndEqual(final T expected) {
            return isPresentAndMatches(actual -> actual.equals(expected));
        }

        public UIPropertyValueMatcher<T> isPresentAndMatches(final Predicate<T> matcher) {
            return new UIPropertyValueMatcher<>(this.property,
                                                maybeActual -> maybeActual.map(matcher::test)
                                                                          .orElse(false));
        }
    }
}
