package fi.jakojaannos.roguelite.engine.view.data.components.ui;

import fi.jakojaannos.roguelite.engine.ecs.Component;

public class Color implements Component {
    public double r;
    public double g;
    public double b;

    public Color(final double r, final double g, final double b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public void set(final Color other) {
        this.r = other.r;
        this.g = other.g;
        this.b = other.b;
    }
}
