package fi.jakojaannos.roguelite.engine.data.components.internal.ui.panel;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class PanelSprite implements Component {
    @Getter @Setter public String sprite;
}
