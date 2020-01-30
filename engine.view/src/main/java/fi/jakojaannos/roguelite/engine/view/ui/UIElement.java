package fi.jakojaannos.roguelite.engine.view.ui;

import java.util.Collection;
import java.util.Optional;

public interface UIElement {
    Optional<UIElement> getParent();

    Collection<UIElement> getChildren();

    default <T> Optional<T> getProperty(final UIProperty<T> property) {
        return property.getFor(this);
    }

    default <T> void setProperty(final UIProperty<T> property, final T value) {
        property.set(this, value);
    }
}
