package fi.jakojaannos.roguelite.engine.view.ui.query;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.internal.query.UIElementMatcherImpl;
import lombok.val;

import java.util.Collection;
import java.util.function.Consumer;

public interface UIElementMatcher extends UIMatcher {
    static UIElementMatcher create() {
        return new UIElementMatcherImpl();
    }

    UIElementMatcher matching(UIMatcher matcher);

    Collection<UIMatcher> evaluateAndGetFailures(final UIElement element);

    default UIPropertyStringMatcherBuilder hasName() {
        return new UIPropertyStringMatcherBuilder(this, UIProperty.NAME);
    }

    default UIPropertyStringMatcherBuilder hasText() {
        return new UIPropertyStringMatcherBuilder(this, UIProperty.TEXT);
    }

    default UIElementMatcher hasChildMatching(final Consumer<UIElementMatcher> builder) {
        val childMatcher = UIElementMatcher.create();
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
