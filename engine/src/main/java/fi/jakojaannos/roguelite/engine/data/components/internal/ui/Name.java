package fi.jakojaannos.roguelite.engine.data.components.internal.ui;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Name implements Component {
    public String value;
}
