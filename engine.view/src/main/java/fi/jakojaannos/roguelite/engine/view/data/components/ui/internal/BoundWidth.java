package fi.jakojaannos.roguelite.engine.view.data.components.ui.internal;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BoundWidth implements Component {
    public ProportionValue value;
}
