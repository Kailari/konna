package fi.jakojaannos.roguelite.engine.view.ui.query;

import java.util.function.Consumer;

public final class UIMatchers {
    private UIMatchers() {
    }

    public static Consumer<UIElementMatcher> withName(final String name) {
        return that -> that.hasName().equalTo(name);
    }
}
