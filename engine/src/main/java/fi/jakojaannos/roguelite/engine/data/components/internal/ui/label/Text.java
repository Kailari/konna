package fi.jakojaannos.roguelite.engine.data.components.internal.ui.label;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class Text implements Component {
    @Getter @Setter public String text;
}
