package fi.jakojaannos.roguelite.engine.view.data.components.internal.panel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import fi.jakojaannos.roguelite.engine.ecs.Component;

@AllArgsConstructor
public class BorderSize implements Component {
    @Getter @Setter public int value;
}
