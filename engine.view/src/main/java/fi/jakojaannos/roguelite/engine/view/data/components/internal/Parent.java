package fi.jakojaannos.roguelite.engine.view.data.components.internal;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;

public class Parent implements Component {
    public Entity value;

    public Parent(final Entity value) {
        this.value = value;
    }
}
