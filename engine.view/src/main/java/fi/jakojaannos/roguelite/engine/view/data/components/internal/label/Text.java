package fi.jakojaannos.roguelite.engine.view.data.components.internal.label;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import fi.jakojaannos.roguelite.engine.ecs.Component;

@AllArgsConstructor
public class Text implements Component {
    @Getter @Setter public String text;
}
