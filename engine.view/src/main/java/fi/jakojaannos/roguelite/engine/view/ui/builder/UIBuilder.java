package fi.jakojaannos.roguelite.engine.view.ui.builder;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;

import java.util.ArrayList;
import java.util.List;

public class UIBuilder {
    private final List<UIElement> elements = new ArrayList<>();

    public <T extends UIElement> UIBuilder withElement(T element) {
        this.elements.add(element);
        return this;
    }

    public List<UIElement> build() {
        return List.copyOf(this.elements);
    }
}
