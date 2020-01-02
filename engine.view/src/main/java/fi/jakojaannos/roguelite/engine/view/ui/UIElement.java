package fi.jakojaannos.roguelite.engine.view.ui;

import java.util.Collection;
import java.util.Optional;

public interface UIElement {
    Optional<UIElement> getParent();

    Collection<UIElement> getChildren();

    default <T> Optional<T> getProperty(UIProperty<T> property) {
        return property.getFor(this);
    }

    default <T> void setProperty(UIProperty<T> property, T value) {
        property.set(this, value);
    }
}
