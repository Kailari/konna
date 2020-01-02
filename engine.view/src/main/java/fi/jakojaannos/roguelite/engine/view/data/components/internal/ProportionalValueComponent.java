package fi.jakojaannos.roguelite.engine.view.data.components.internal;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;

public interface ProportionalValueComponent extends Component {
    ProportionValue getValue();

    void setValue(ProportionValue value);
}
