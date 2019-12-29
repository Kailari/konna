package fi.jakojaannos.roguelite.engine.data.components.internal.ui;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ui.ProportionValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BoundAnchorY implements Component {
    public ProportionValue value;
}
