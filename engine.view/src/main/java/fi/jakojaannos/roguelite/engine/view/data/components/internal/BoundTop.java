package fi.jakojaannos.roguelite.engine.view.data.components.internal;

import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class BoundTop implements ProportionalValueComponent {
    @Getter @Setter public ProportionValue value;
}
