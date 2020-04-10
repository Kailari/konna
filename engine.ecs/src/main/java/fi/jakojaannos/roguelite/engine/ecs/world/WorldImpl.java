package fi.jakojaannos.roguelite.engine.ecs.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.LogCategories;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.world.storage.ComponentStorage;
import fi.jakojaannos.roguelite.engine.ecs.world.storage.ResourceStorage;

public class WorldImpl implements World {
    private static final Logger LOG = LoggerFactory.getLogger(World.class);

    @Deprecated
    private final LegacyCompat compat;

    private final ComponentStorage componentStorage;
    private final ResourceStorage resourceStorage;
    private final List<Runnable> entityRemoveTasks = new ArrayList<>();

    private EntityHandleImpl[] entities;
    private int capacity;

    private int idCounter;
    private int nEntities;

    @Override
    public LegacyCompat getCompatibilityLayer() {
        return this.compat;
    }

    @Override
    public int getEntityCount() {
        return this.nEntities;
    }

    @Override
    public ComponentStorage getComponents() {
        return this.componentStorage;
    }

    @Override
    public ResourceStorage getResources() {
        return this.resourceStorage;
    }

    public WorldImpl() {
        this.capacity = 256;
        this.idCounter = 0;
        this.nEntities = 0;

        this.entities = new EntityHandleImpl[this.capacity];
        this.resourceStorage = new ResourceStorage();
        this.componentStorage = new ComponentStorage(this.capacity);

        this.compat = new LegacyCompat(this);
    }

    @Override
    public void registerResource(final Object resource) {
        this.resourceStorage.register(resource);
    }

    @Override
    public <TResource> void registerResource(final Class<? super TResource> resourceClass, final TResource resource) {
        this.resourceStorage.register(resourceClass, resource);
    }

    @Override
    public EntityHandle createEntity(final Object... components) {
        final var id = this.idCounter;
        this.idCounter++;

        LOG.debug(LogCategories.ENTITY_LIFECYCLE, "Creating entity with id {}", id);
        if (id == this.entities.length) {
            resize(this.capacity + 256);
        }

        // TODO: Change to EntityHandleImpl
        this.entities[id] = new LegacyEntityHandleImpl(id, this);
        for (final var component : components) {
            this.entities[id].addComponent(component);
        }

        return this.entities[id];
    }

    private void resize(final int newCapacity) {
        this.capacity = newCapacity;
        this.entities = Arrays.copyOf(this.entities, newCapacity);
        this.componentStorage.resize(newCapacity);
    }

    @Override
    public EntityHandle getEntity(final int entityId) {
        return this.entities[entityId];
    }

    @Override
    public void destroyEntity(final EntityHandle handle) {
        LOG.trace(LogCategories.ENTITY_LIFECYCLE, "Marking entity {} for removal", handle);
        final var actualHandle = (EntityHandleImpl) handle;
        actualHandle.markPendingRemoval();

        this.entityRemoveTasks.add(() -> {
            this.idCounter--;
            this.nEntities--;
            actualHandle.markDestroyed();

            final var removedSlot = actualHandle.getId();
            LOG.debug(LogCategories.ENTITY_LIFECYCLE, "Destroyed entity {}", removedSlot);

            // Swap components from the last entity to the removed slot
            this.componentStorage.moveAndClear(this.idCounter, removedSlot);
            if (removedSlot != this.idCounter) {

                // Swap handle IDs
                this.entities[this.idCounter].moveTo(removedSlot);
                this.entities[removedSlot].moveTo(-1);

                // Swap handle to correct position in lookup
                this.entities[removedSlot] = this.entities[this.idCounter];
            }
            this.entities[this.idCounter] = null;
        });
    }

    @Override
    public void clearAllEntities() {
        LOG.warn(LogCategories.ENTITY_LIFECYCLE, "Clearing all ({}) entities!", this.nEntities);
        this.entityRemoveTasks.clear();
        this.componentStorage.clear();
        for (int i = 0; i < this.entities.length; i++) {
            if (this.entities[i] != null) {
                this.entities[i].markPendingRemoval();
                this.entities[i].markDestroyed();
                this.entities[i].moveTo(-1);
            }

            this.entities[i] = null;
        }
        this.idCounter = 0;
        this.nEntities = 0;
    }

    @Override
    public void commitEntityModifications() {
        this.nEntities = this.idCounter;
        this.entityRemoveTasks.forEach(Runnable::run);
        this.entityRemoveTasks.clear();
    }

    @Override
    public <TResource> TResource fetchResource(final Class<TResource> resourceClass) {
        return this.resourceStorage.fetch(resourceClass);
    }
}
