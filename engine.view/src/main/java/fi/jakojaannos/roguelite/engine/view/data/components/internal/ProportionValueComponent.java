package fi.jakojaannos.roguelite.engine.view.data.components.internal;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Component;
import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;

public interface ProportionValueComponent extends Component {
    ProportionValue getValue();

    void setValue(ProportionValue value);
}
