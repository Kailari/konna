package fi.jakojaannos.roguelite.engine.view.ui.query;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;

public interface UIPropertyMatcher<T> extends UIMatcher {
    static <T> UIPropertyValueMatcher.Builder<T> match(final UIProperty<T> property) {
        return new UIPropertyValueMatcher.Builder<>(property);
    }

    @RequiredArgsConstructor
    class Builder<T, M extends UIPropertyMatcher<T>> {
        @Getter(AccessLevel.PROTECTED)
        private final UIElementMatcher parent;
    }
}
