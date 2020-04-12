package fi.jakojaannos.roguelite.engine.view.data.components.internal;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;

public class Parent {
    public EntityHandle value;

    public Parent(final EntityHandle value) {
        this.value = value;
    }
}
