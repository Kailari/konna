package fi.jakojaannos.roguelite.engine.view.ui.query;

import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public class UIPropertyStringMatcherBuilder {
    private final UIElementMatcher parent;
    private final UIProperty<String> property;

    public UIElementMatcher whichContains(final String... strings) {
        return this.parent.matching(UIPropertyMatcher.match(this.property)
                                                     .ifPresentOrElse(actual -> Arrays.stream(strings).anyMatch(actual::contains),
                                                                      () -> strings.length == 0 || Arrays.stream(strings).allMatch(String::isEmpty)));
    }

    public UIElementMatcher equalTo(final String string) {
        return this.parent.matching(UIPropertyMatcher.match(this.property)
                                                     .ifPresentOrElse(actual -> actual.equals(string),
                                                                      string::isEmpty));
    }

    public UIElementMatcher whichStartsWith(final String string) {
        return this.parent.matching(UIPropertyMatcher.match(this.property)
                                                     .ifPresentOrElse(actual -> actual.startsWith(string),
                                                                      string::isEmpty));
    }
}
