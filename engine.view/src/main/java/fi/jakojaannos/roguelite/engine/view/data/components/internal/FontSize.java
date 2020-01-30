package fi.jakojaannos.roguelite.engine.view.data.components.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import fi.jakojaannos.roguelite.engine.ecs.Component;

@AllArgsConstructor
public class FontSize implements Component {
    @Getter @Setter public int value;
}
