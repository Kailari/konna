package fi.jakojaannos.roguelite.engine.ecs.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.LogCategories;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.data.resources.Entities;
import fi.jakojaannos.roguelite.engine.ecs.dispatcher.EntitySpliterator;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.world.storage.ComponentStorage;
import fi.jakojaannos.roguelite.engine.ecs.world.storage.ResourceStorage;

public class WorldImpl implements World {
    private static final Logger LOG = LoggerFactory.getLogger(World.class);

    @Deprecated
    private final LegacyCompat legacyCompatibilityEntityManager;

    private final ComponentStorage componentStorage;
    private final ResourceStorage resourceStorage;
    private final List<Runnable> entityRemoveTasks = new ArrayList<>();

    private final AtomicInteger idCounter = new AtomicInteger(0);
    private final AtomicInteger nEntities = new AtomicInteger(0);

    private EntityHandleImpl[] entities;
    private int capacity;

    @Override
    public EntityManager getEntityManager() {
        return this.legacyCompatibilityEntityManager;
    }

    @Override
    public int getEntityCount() {
        return this.nEntities.get();
    }

    public ComponentStorage getComponentStorage() {
        return this.componentStorage;
    }

    public WorldImpl() {
        this.capacity = 256;
        this.idCounter.set(0);
        this.nEntities.set(0);

        this.entities = new EntityHandleImpl[this.capacity];
        this.resourceStorage = new ResourceStorage();
        this.componentStorage = new ComponentStorage(this.capacity);

        this.registerResource(Entities.class, this::createEntity);

        this.legacyCompatibilityEntityManager = new LegacyCompat(this);
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
    public synchronized EntityHandle createEntity(final Object... components) {
        final var id = this.idCounter.getAndAdd(1);

        LOG.debug(LogCategories.ENTITY_LIFECYCLE, "Creating entity with id {}", id);
        if (id == this.entities.length) {
            resize(this.capacity + 256);
        }

        // TODO: Change to EntityHandleImpl
        this.entities[id] = new LegacyEntityHandleImpl(id, this);
        for (final var component : components) {
            if (!this.entities[id].addComponent(component)) {
                throw new IllegalStateException("Tried adding component " + component.getClass().getSimpleName()
                                                + " to an entity, but the component already exists!");
            }
        }

        return this.entities[id];
    }

    private synchronized void resize(final int newCapacity) {
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

        this.entityRemoveTasks.add(() -> cleanUpEntity(actualHandle));
    }

    private synchronized void cleanUpEntity(final EntityHandleImpl actualHandle) {
        this.idCounter.decrementAndGet();
        this.nEntities.decrementAndGet();
        actualHandle.markDestroyed();

        final var removedSlot = actualHandle.getId();
        LOG.debug(LogCategories.ENTITY_LIFECYCLE, "Destroyed entity {}", removedSlot);

        // Swap components from the last entity to the removed slot
        this.componentStorage.moveAndClear(this.idCounter.get(), removedSlot);
        if (removedSlot != this.idCounter.get()) {

            // Swap handle IDs
            this.entities[this.idCounter.get()].moveTo(removedSlot);
            this.entities[removedSlot].moveTo(-1);

            // Swap handle to correct position in lookup
            this.entities[removedSlot] = this.entities[this.idCounter.get()];
        }
        this.entities[this.idCounter.get()] = null;
    }

    @Override
    public synchronized void clearAllEntities() {
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
        this.idCounter.set(0);
        this.nEntities.set(0);
    }

    @Override
    public synchronized void commitEntityModifications() {
        this.nEntities.set(this.idCounter.get());
        this.entityRemoveTasks.forEach(Runnable::run);
        this.entityRemoveTasks.clear();
    }

    @Override
    public <TResource> TResource fetchResource(final Class<TResource> resourceClass) {
        return this.resourceStorage.fetch(resourceClass);
    }

    @Override
    public Object[] fetchResources(final Class<?>[] resourceClasses) {
        return this.resourceStorage.fetch(resourceClasses);
    }

    @Override
    public <TEntityData> Spliterator<EntityDataHandle<TEntityData>> iterateEntities(
            final Class<?>[] componentClasses,
            final boolean[] excluded,
            final boolean[] optional,
            final Function<Object[], TEntityData> dataFactory
    ) {
        return new EntitySpliterator<>(componentClasses, excluded, optional, this, dataFactory);
    }
}
