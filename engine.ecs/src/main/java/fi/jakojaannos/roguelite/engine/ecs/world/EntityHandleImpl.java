package fi.jakojaannos.roguelite.engine.ecs.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Supplier;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;

public class EntityHandleImpl implements EntityHandle {
    private static final Logger LOG = LoggerFactory.getLogger(EntityHandleImpl.class);

    private final WorldImpl world;
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

    @Override
    public boolean isDestroyed() {
        return this.destroyed;
    }

    public EntityHandleImpl(final int id, final WorldImpl world) {
        this.id = id;
        this.world = world;
    }

    @Override
    public <TComponent> boolean addComponent(final TComponent component) {
        return this.world.getComponents().add(this.id, component);
    }

    @Override
    public <TComponent> TComponent addOrGet(
            final Class<TComponent> componentClass,
            final Supplier<TComponent> supplier
    ) {
        return this.world.getComponents().addOrGet(this.id, componentClass, supplier);
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
        if (this.destroyed) {
            throw new IllegalStateException("Already destroyed!");
        }

        if (this.pendingRemoval) {
            LOG.warn("Already marked for removal!");
            return;
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
