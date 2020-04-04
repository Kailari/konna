package fi.jakojaannos.roguelite.engine.ecs.newimpl.world;

import java.util.Optional;

import fi.jakojaannos.roguelite.engine.ecs.newimpl.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.components.ComponentStorage;

public class EntityHandleImpl implements EntityHandle {
    private final int id;
    private final ComponentStorage componentStorage;

    @Override
    public int getId() {
        return this.id;
    }

    public EntityHandleImpl(final int id, final ComponentStorage componentStorage) {
        this.id = id;
        this.componentStorage = componentStorage;
    }

    @Override
    public <TComponent> boolean addComponent(final TComponent component) {
        return this.componentStorage.add(this.id, component);
    }

    @Override
    public <TComponent> boolean removeComponent(final Class<TComponent> componentClass) {
        return false;
    }

    @Override
    public <TComponent> boolean hasComponent(final Class<TComponent> componentClass) {
        return false;
    }

    @Override
    public <TComponent> Optional<TComponent> getComponent(final Class<TComponent> componentClass) {
        return this.componentStorage.get(this.id, componentClass);
    }

    @Override
    public void destroy() {

    }
}
