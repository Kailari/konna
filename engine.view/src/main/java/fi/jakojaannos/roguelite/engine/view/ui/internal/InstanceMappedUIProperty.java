package fi.jakojaannos.roguelite.engine.view.ui.internal;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InstanceMappedUIProperty<T> implements UIProperty<T> {
    private final String name;
    private final T defaultValue;
    private final Map<UIElement, T> values = new HashMap<>();

    public InstanceMappedUIProperty(final String name, final T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Optional<T> getFor(final UIElement uiElement) {
        return Optional.of(this.values.getOrDefault(uiElement, this.defaultValue));
    }

    @Override
    public void set(final UIElement uiElement, final T value) {
        this.values.put(uiElement, value);
    }
}
