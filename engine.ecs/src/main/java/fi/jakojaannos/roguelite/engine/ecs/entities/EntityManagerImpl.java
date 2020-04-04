package fi.jakojaannos.roguelite.engine.ecs.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.components.ComponentStorage;
import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;

/**
 * Default {@link EntityManager} implementation.
 */
public class EntityManagerImpl implements EntityManager {
    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerImpl.class);

    private final int maxComponentTypes;
    private final EntityStorage entityStorage;
    private final ComponentStorage componentStorage;
    private final Queue<StorageTask> taskQueue = new ArrayDeque<>();

    private int entityCapacity;

    @Override
    public Stream<Entity> getAllEntities() {
        return this.entityStorage.stream().map(Entity.class::cast);
    }

    public EntityManagerImpl(final int entityCapacity, final int maxComponentTypes) {
        this(entityCapacity, maxComponentTypes, new EntityStorage(entityCapacity),
             new ComponentStorage(entityCapacity, maxComponentTypes));
    }

    public EntityManagerImpl(
            final int entityCapacity,
            final int maxComponentTypes,
            final EntityStorage entityStorage,
            final ComponentStorage componentStorage
    ) {
        this.entityStorage = entityStorage;
        this.entityCapacity = entityCapacity;
        this.maxComponentTypes = maxComponentTypes;
        this.componentStorage = componentStorage;
    }

    @Override
    public Entity createEntity() {
        final var entity = this.entityStorage.create(this.maxComponentTypes);
        if (this.entityStorage.isFull()) {
            resize(this.entityCapacity * 2);
        }

        this.taskQueue.offer(() -> this.entityStorage.spawn(entity));

        return entity;
    }

    @Override
    public void destroyEntity(final Entity entityRaw) {
        final var entity = (EntityImpl) entityRaw;
        entity.markForRemoval();
        this.taskQueue.offer(() -> {
            this.componentStorage.clear(entity);
            this.entityStorage.remove(entity);
        });
    }

    @Override
    public void applyModifications() {
        while (!this.taskQueue.isEmpty()) {
            this.taskQueue.remove().execute();
        }
    }

    @Override
    public <TComponent extends Component> TComponent addComponentTo(
            final Entity entity,
            final TComponent component
    ) {
        this.componentStorage.add((EntityImpl) entity, component);
        return component;
    }

    @Override
    public void removeComponentFrom(
            final Entity entity,
            final Class<? extends Component> componentClass
    ) {
        this.componentStorage.remove((EntityImpl) entity, componentClass);
    }

    @Override
    public <TComponent extends Component> Optional<TComponent> getComponentOf(
            final Entity entity,
            final Class<TComponent> componentClass
    ) {
        return this.componentStorage.get((EntityImpl) entity, componentClass);
    }

    @Override
    public boolean hasComponent(
            final Entity entity,
            final Class<? extends Component> componentClass
    ) {
        return this.componentStorage.exists((EntityImpl) entity, componentClass);
    }

    @Override
    public <TComponent extends Component> Stream<EntityComponentPair<TComponent>> getEntitiesWith(
            final Class<? extends TComponent> componentClass
    ) {
        return this.entityStorage.stream()
                                 .filter(e -> this.componentStorage.exists(e, componentClass))
                                 .map(e -> new EntityComponentPair<>(e, getComponentOf(e, componentClass)
                                         .orElseThrow()));
    }

    @Override
    public Stream<Entity> getEntitiesWith(
            final Collection<Class<? extends Component>> required,
            final Collection<Class<? extends Component>> excluded
    ) {
        final var requiredMask = this.componentStorage.createComponentBitmask(required);
        final var excludedMask = this.componentStorage.createComponentBitmask(excluded);
        return this.entityStorage.stream()
                                 .filter(e -> BitMaskUtils.hasNoneOfTheBitsOf(e.getComponentBitmask(), excludedMask))
                                 .filter(e -> BitMaskUtils.hasAllBitsOf(e.getComponentBitmask(), requiredMask))
                                 .map(Entity.class::cast);
    }

    private void resize(final int entityCapacity) {
        LOG.debug("Resizing EntityManager... Size: {} -> {}", this.entityCapacity, entityCapacity);
        this.entityCapacity = entityCapacity;
        this.entityStorage.resize(entityCapacity);
        this.componentStorage.resize(entityCapacity);
    }

    private interface StorageTask {
        void execute();
    }
}
