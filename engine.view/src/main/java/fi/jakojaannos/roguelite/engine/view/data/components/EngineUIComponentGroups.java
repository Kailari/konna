package fi.jakojaannos.roguelite.engine.view.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ComponentGroup;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.internal.*;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public enum EngineUIComponentGroups implements ComponentGroup {
    ELEMENT_BOUND(List.of(BoundBottom.class, BoundTop.class, BoundLeft.class, BoundRight.class, BoundWidth.class, BoundHeight.class));

    private final Collection<Class<? extends Component>> componentTypes;

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public Collection<Class<? extends Component>> getComponentTypes() {
        return this.componentTypes;
    }
}
