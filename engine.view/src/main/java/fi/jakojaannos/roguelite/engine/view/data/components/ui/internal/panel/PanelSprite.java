package fi.jakojaannos.roguelite.engine.view.data.components.ui.internal.panel;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.view.sprite.Sprite;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PanelSprite implements Component {
    public Sprite sprite;
}
