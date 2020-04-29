package fi.jakojaannos.roguelite.engine.view.ui.query;

import java.util.Collection;
import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.internal.query.UIElementMatcherImpl;

public interface UIElementMatcher extends UIMatcher {
    static UIElementMatcher create() {
        return new UIElementMatcherImpl();
    }

    UIElementMatcher matching(UIMatcher matcher);

    /**
     * Evaluates the matcher on given element, keeping track of all failed UI matchers
     *
     * @param element element to match against
     *
     * @return collection containing all failed matchers
     */
    Collection<UIMatcher> evaluateAndGetFailures(UIElement element);

    @Override
    default boolean evaluate(final UIElement element) {
        return evaluateAndGetFailures(element).isEmpty();
    }

    default UIPropertyStringMatcherBuilder hasName() {
        return new UIPropertyStringMatcherBuilder(this, UIProperty.NAME);
    }

    default UIPropertyStringMatcherBuilder hasText() {
        return new UIPropertyStringMatcherBuilder(this, UIProperty.TEXT);
    }

    default UIElementMatcher hasChildMatching(final Consumer<UIElementMatcher> builder) {
        final var childMatcher = UIElementMatcher.create();
        builder.accept(childMatcher);
        return matching(element -> element.getChildren().stream()
                                          .anyMatch(childMatcher::evaluate));
    }

    default UIElementMatcher isVisible() {
        return matching(UIPropertyMatcher.match(UIProperty.HIDDEN).isUndefinedOr(false));
    }

    default UIElementMatcher isHidden() {
        return matching(UIPropertyMatcher.match(UIProperty.HIDDEN).isPresentAndEqual(true));
    }

    default UIElementMatcher isProgressBar() {
        return matching(UIPropertyMatcher.match(UIProperty.PROGRESS).isPresent());
    }

    default UIElementMatcher isLabel() {
        return matching(UIPropertyMatcher.match(UIProperty.TEXT).isPresent());
    }
}
