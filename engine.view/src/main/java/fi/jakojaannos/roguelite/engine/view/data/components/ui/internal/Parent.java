package fi.jakojaannos.roguelite.engine.view.data.components.ui.internal;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Parent implements Component {
    public Entity value;
}
