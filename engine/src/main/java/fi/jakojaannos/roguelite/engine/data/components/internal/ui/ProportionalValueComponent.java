package fi.jakojaannos.roguelite.engine.data.components.internal.ui;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ui.ProportionValue;

public interface ProportionalValueComponent extends Component {
    ProportionValue getValue();

    void setValue(ProportionValue value);
}
