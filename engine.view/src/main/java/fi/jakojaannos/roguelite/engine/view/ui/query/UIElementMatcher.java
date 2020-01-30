package fi.jakojaannos.roguelite.engine.view.ui.query;

import java.util.Collection;
import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.internal.query.UIElementMatcherImpl;

public interface UIElementMatcher extends UIMatcher {
    static UIElementMatcher create() {
        return new UIElementMatcherImpl();
    }

    UIElementMatcher matching(UIMatcher matcher);

    Collection<UIMatcher> evaluateAndGetFailures(UIElement element);

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
        return matching(UIPropertyMatcher.match(UIProperty.TYPE).isPresentAndEqual(UIElementType.PROGRESS_BAR));
    }

    default UIElementMatcher isLabel() {
        return matching(UIPropertyMatcher.match(UIProperty.TYPE).isPresentAndEqual(UIElementType.LABEL));
    }

    @Override
    default boolean evaluate(final UIElement element) {
        return evaluateAndGetFailures(element).isEmpty();
    }
}
