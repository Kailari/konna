package fi.jakojaannos.roguelite.engine.data.components.internal.ui.label;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Text implements Component {
    public String text;
}
