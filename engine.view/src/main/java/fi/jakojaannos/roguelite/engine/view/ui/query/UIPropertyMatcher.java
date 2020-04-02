package fi.jakojaannos.roguelite.engine.view.ui.query;

import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;

public interface UIPropertyMatcher<T> extends UIMatcher {
    static <T> UIPropertyValueMatcher.Builder<T> match(final UIProperty<T> property) {
        return new UIPropertyValueMatcher.Builder<>(property);
    }

    class Builder<T, M extends UIPropertyMatcher<T>> {
        private final UIElementMatcher parent;

        protected UIElementMatcher getParent() {
            return this.parent;
        }

        Builder(final UIElementMatcher parent) {
            this.parent = parent;
        }
    }
}
