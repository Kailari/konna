package fi.jakojaannos.roguelite.engine.ecs.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.LogCategories;
import fi.jakojaannos.roguelite.engine.ecs.world.storage.EntityChunk;
import fi.jakojaannos.roguelite.engine.ecs.world.storage.EntityStorage;

public class EntityHandleImpl implements EntityHandle {
    private static final Logger LOG = LoggerFactory.getLogger(EntityHandleImpl.class);

    private final EntityStorage storage;
    private final int stableId;

    private EntityChunk chunk;
    private int storageIndex;
    private boolean pendingRemoval;
    private boolean destroyed;

    @Override
    public int getId() {
        return this.stableId;
    }

    @Override
    public boolean isPendingRemoval() {
        return this.pendingRemoval;
    }

    @Override
    public boolean isDestroyed() {
        return this.destroyed;
    }

    public EntityChunk getChunk() {
        return this.chunk;
    }

    public int getStorageIndex() {
        return this.storageIndex;
    }

    public EntityHandleImpl(
            final int id,
            final EntityStorage storage
    ) {
        this.stableId = id;
        this.storage = storage;
        this.storageIndex = -1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TComponent> boolean addComponent(final TComponent component) {
        if (this.destroyed) {
            throw new IllegalStateException("Tried adding component to a destroyed entity!");
        }
        return this.storage.addComponent(this,
                                         (Class<? super TComponent>) component.getClass(),
                                         component);
    }

    @Override
    public <TComponent> boolean removeComponent(final Class<TComponent> componentClass) {
        if (this.destroyed) {
            throw new IllegalStateException("Tried removing component from a destroyed entity!");
        }
        return this.storage.removeComponent(this, componentClass);
    }

    @Override
    public <TComponent> boolean hasComponent(final Class<TComponent> componentClass) {
        if (this.destroyed) {
            throw new IllegalStateException("Tried querying component from a destroyed entity!");
        }
        return this.chunk.getArchetype()
                         .hasComponent(componentClass);
    }

    @Override
    public <TComponent> Optional<TComponent> getComponent(final Class<TComponent> componentClass) {
        if (this.destroyed) {
            throw new IllegalStateException("Tried getting component from a destroyed entity!");
        }

        if (!hasComponent(componentClass)) {
            return Optional.empty();
        }

        return Optional.of(this.chunk.getComponent(this.storageIndex, componentClass));
    }

    @Override
    public void destroy() {
        LOG.trace(LogCategories.ENTITY_LIFECYCLE, "Marking entity {} for removal", this);

        if (this.destroyed) {
            throw new IllegalStateException("Tried destroying an already destroyed entity!");
        }

        if (this.pendingRemoval) {
            LOG.warn("Already marked for removal!");
            return;
        }

        this.pendingRemoval = true;
        this.storage.destroyEntity(this);
    }

    public void markDestroyed() {
        if (!this.pendingRemoval) {
            LOG.warn("Entity destroyed before it was marked pending for removal!");
        }

        if (this.destroyed) {
            throw new IllegalStateException("Already destroyed!");
        }

        this.destroyed = true;
    }

    public void moveToChunk(final EntityChunk chunk, final int storageIndex) {
        this.chunk = chunk;
        this.storageIndex = storageIndex;
    }
}
