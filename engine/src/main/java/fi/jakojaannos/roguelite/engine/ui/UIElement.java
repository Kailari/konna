package fi.jakojaannos.roguelite.engine.ui;

import java.util.Collection;
import java.util.Optional;

public interface UIElement {
    Optional<UIElement> getParent();

    Collection<UIElement> getChildren();

    <T> Optional<T> getProperty(UIProperty<T> property);
}
