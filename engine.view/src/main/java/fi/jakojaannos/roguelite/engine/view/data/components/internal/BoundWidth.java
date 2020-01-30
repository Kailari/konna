package fi.jakojaannos.roguelite.engine.view.data.components.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;

@AllArgsConstructor
public class BoundWidth implements ProportionValueComponent {
    @Getter @Setter public ProportionValue value;
}
