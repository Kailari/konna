package fi.jakojaannos.roguelite.engine.view.ui.internal.query;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.query.UIElementMatcher;
import fi.jakojaannos.roguelite.engine.view.ui.query.UIMatcher;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UIElementMatcherImpl implements UIElementMatcher {
    private List<UIMatcher> matchers = new ArrayList<>();

    @Override
    public UIElementMatcher matching(final UIMatcher matcher) {
        this.matchers.add(matcher);
        return this;
    }

    @Override
    public Collection<UIMatcher> evaluateAndGetFailures(final UIElement element) {
        return this.matchers.stream()
                            .filter(matcher -> !matcher.evaluate(element))
                            .collect(Collectors.toUnmodifiableList());
    }
}
