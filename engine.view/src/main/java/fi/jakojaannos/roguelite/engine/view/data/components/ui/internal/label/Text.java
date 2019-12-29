package fi.jakojaannos.roguelite.engine.view.data.components.ui.internal.label;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.view.text.Font;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Text implements Component {
    public String text;
    public Font font;
}
