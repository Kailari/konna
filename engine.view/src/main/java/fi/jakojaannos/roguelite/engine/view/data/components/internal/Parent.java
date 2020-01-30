package fi.jakojaannos.roguelite.engine.view.data.components.internal;

import lombok.AllArgsConstructor;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;

@AllArgsConstructor
public class Parent implements Component {
    public Entity value;
}
