package fi.jakojaannos.roguelite.engine.data.components.internal.ui;

import fi.jakojaannos.roguelite.engine.ui.ProportionValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class BoundHeight implements ProportionalValueComponent {
    @Getter @Setter public ProportionValue value;
}
