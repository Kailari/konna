package fi.jakojaannos.roguelite.engine.ecs.newecs.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import fi.jakojaannos.roguelite.engine.ecs.newecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.newecs.World;

public class EntityHandleImpl implements EntityHandle {
    private static final Logger LOG = LoggerFactory.getLogger(EntityHandleImpl.class);

    private final World world;
    private int id;
    private boolean pendingRemoval;
    private boolean destroyed;

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public boolean isPendingRemoval() {
        return this.pendingRemoval;
    }

    public EntityHandleImpl(final int id, final World world) {
        this.id = id;
        this.world = world;
    }

    @Override
    public <TComponent> boolean addComponent(final TComponent component) {
        return this.world.getComponents().add(this.id, component);
    }

    @Override
    public <TComponent> boolean removeComponent(final Class<TComponent> componentClass) {
        return this.world.getComponents().remove(this.id, componentClass);
    }

    @Override
    public <TComponent> boolean hasComponent(final Class<TComponent> componentClass) {
        return this.world.getComponents().has(this.id, componentClass);
    }

    @Override
    public <TComponent> Optional<TComponent> getComponent(final Class<TComponent> componentClass) {
        return this.world.getComponents().get(this.id, componentClass);
    }

    @Override
    public void destroy() {
        if (this.pendingRemoval) {
            LOG.warn("Already marked for removal!");
            return;
        }

        if (this.destroyed) {
            throw new IllegalStateException("Already destroyed!");
        }
        this.world.destroyEntity(this);
    }

    public void markDestroyed() {
        if (this.destroyed) {
            throw new IllegalStateException("Already destroyed!");
        }

        this.destroyed = true;
    }

    public void markPendingRemoval() {
        this.pendingRemoval = true;
    }

    public void moveTo(final int slot) {
        this.id = slot;
    }
}
