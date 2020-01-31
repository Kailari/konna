package fi.jakojaannos.roguelite.engine.view.data.components.ui;

import lombok.AllArgsConstructor;

import fi.jakojaannos.roguelite.engine.ecs.Component;

@AllArgsConstructor
public class Color implements Component {
    public double r;
    public double g;
    public double b;

    public void set(final Color other) {
        this.r = other.r;
        this.g = other.g;
        this.b = other.b;
    }
}
