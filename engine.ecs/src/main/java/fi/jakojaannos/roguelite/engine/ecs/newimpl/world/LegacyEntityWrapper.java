package fi.jakojaannos.roguelite.engine.ecs.newimpl.world;

import java.util.Optional;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.EntityHandle;

public class LegacyEntityWrapper implements Entity {
    private final EntityHandle entityHandle;

    private boolean markedForRemoval;

    @Override
    public int getId() {
        return this.entityHandle.getId();
    }

    @Override
    public boolean isMarkedForRemoval() {
        return this.markedForRemoval;
    }

    public LegacyEntityWrapper(final EntityHandle entityHandle) {
        this.entityHandle = entityHandle;
    }

    public <TComponent extends Component> TComponent addComponent(final TComponent component) {
        this.entityHandle.addComponent(component);
        return component;
    }

    public void removeComponent(final Class<? extends Component> componentClass) {
        this.entityHandle.removeComponent(componentClass);
    }

    public <TComponent extends Component> Optional<TComponent> getComponent(
            final Class<TComponent> componentClass
    ) {
        return this.entityHandle.getComponent(componentClass);
    }

    public boolean hasComponent(final Class<? extends Component> componentClass) {
        return this.entityHandle.hasComponent(componentClass);
    }
}
