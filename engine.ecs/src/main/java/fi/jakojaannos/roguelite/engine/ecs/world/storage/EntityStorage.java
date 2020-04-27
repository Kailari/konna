package fi.jakojaannos.roguelite.engine.ecs.world.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.LogCategories;
import fi.jakojaannos.roguelite.engine.ecs.world.EntityHandleImpl;
import fi.jakojaannos.roguelite.engine.ecs.world.LegacyEntityHandleImpl;

public class EntityStorage {
    private static final Logger LOG = LoggerFactory.getLogger(EntityStorage.class);

    private final Collection<Archetype> archetypes = new ArrayList<>();
    private final AtomicInteger idCounter = new AtomicInteger(0);

    public <TEntityData> Stream<EntityDataHandle<TEntityData>> stream(
            final Class<?>[] componentClasses,
            final boolean[] excluded,
            final boolean[] optional,
            final Function<Object[], TEntityData> dataFactory,
            final boolean parallel
    ) {
        // Component classes without excluded components
        final var requiredClasses = IntStream.range(0, componentClasses.length)
                                             .filter(i -> !excluded[i])
                                             .mapToObj(i -> componentClasses[i])
                                             .toArray(Class[]::new);

        // Filter by exclusion and flatten per-archetype streams to a single stream
        final Stream<Archetype> baseStream;
        final var immutableArchetypes = List.copyOf(this.archetypes);
        if (parallel) {
            baseStream = immutableArchetypes.parallelStream();
        } else {
            baseStream = immutableArchetypes.stream();
        }
        return baseStream.filter(archetype -> archetype.matchesRequirements(componentClasses,
                                                                            excluded,
                                                                            optional))
                         .flatMap(archetype -> archetype.stream(requiredClasses,
                                                                optional,
                                                                dataFactory,
                                                                parallel));
    }

    /**
     * Adds the given component to the entity. Does nothing if the entity already has the given component.
     * <p>
     * This requires moving the entity to another archetype with all the components the entity previously had and the
     * newly added component. However, as iterating the storage might still be in progress, the entity slot where the
     * entity previously resided cannot be released until ticking the current system ends.
     *
     * @param entityHandle   the entity to add the component to
     * @param componentClass class of the added component
     * @param component      the component to add
     * @param <TComponent>   type of the added component
     *
     * @return <code>true</code> if the component was added
     */
    public <TComponent> boolean addComponent(
            final EntityHandleImpl entityHandle,
            final Class<TComponent> componentClass,
            final TComponent component
    ) {
        // Check if we need to do something
        final var chunk = entityHandle.getChunk();
        final var archetype = chunk.getArchetype();
        if (archetype.hasComponent(componentClass)) {
            // Entity already has the component, do nothing and return false
            return false;
        }

        // Find or create the new archetype
        final var oldComponents = archetype.getComponentClasses();
        final var componentClasses = new Class[oldComponents.length + 1];
        System.arraycopy(oldComponents, 0, componentClasses, 0, oldComponents.length);
        componentClasses[componentClasses.length - 1] = component.getClass();

        final var newArchetype = findOrCreateArchetype(componentClasses);

        // Copy to the new archetype
        final var storages = chunk.fetchStorages(oldComponents, new boolean[oldComponents.length]);
        final var components = new Object[componentClasses.length];
        for (int i = 0; i < oldComponents.length; i++) {
            components[i] = storages[i][entityHandle.getStorageIndex()];
        }
        components[components.length - 1] = component;
        final var oldIndex = entityHandle.getStorageIndex();
        newArchetype.addEntity(entityHandle, componentClasses, components);

        // Queue for removal from the old archetype
        archetype.queueRemoved(chunk, oldIndex, false);

        // The component was added, return true
        return true;
    }

    public <TComponent> boolean removeComponent(
            final EntityHandleImpl entityHandle,
            final Class<TComponent> componentClass
    ) {
        // Check if we need to do something
        final var chunk = entityHandle.getChunk();
        final var archetype = chunk.getArchetype();
        if (!archetype.hasComponent(componentClass)) {
            // Entity does not have the component, do nothing and return false
            return false;
        }

        // Find or create the new archetype
        final var oldComponents = archetype.getComponentClasses();
        final var componentClasses = new Class[oldComponents.length - 1];
        for (int i = 0, iOld = 0; i < componentClasses.length; ++i, ++iOld) {
            if (oldComponents[i].equals(componentClass)) {
                ++iOld;
            }
            componentClasses[i] = oldComponents[iOld];
        }

        final var newArchetype = findOrCreateArchetype(componentClasses);

        // Move to the new archetype
        final var storages = chunk.fetchStorages(componentClasses, new boolean[componentClasses.length]);
        final var components = new Object[componentClasses.length];
        for (int i = 0; i < components.length; i++) {
            components[i] = storages[i][entityHandle.getStorageIndex()];
        }
        final var oldIndex = entityHandle.getStorageIndex();
        newArchetype.addEntity(entityHandle, componentClasses, components);

        // Queue for removal from the old archetype
        archetype.queueRemoved(chunk, oldIndex, false);

        // The component was removed, return true
        return true;
    }

    private Archetype findOrCreateArchetype(final Class<?>[] componentClasses) {
        for (final var archetype : this.archetypes) {
            final var hasCorrectNumOfComponents = archetype.getComponentCount() == componentClasses.length;

            if (hasCorrectNumOfComponents && archetype.hasAll(componentClasses)) {
                return archetype;
            }
        }

        final var archetype = new Archetype(componentClasses);
        this.archetypes.add(archetype);
        return archetype;
    }

    public void commitModifications() {
        for (final var archetype : this.archetypes) {
            archetype.commitModifications();
        }
    }

    public void destroyEntity(final EntityHandleImpl entityHandle) {
        final var chunk = entityHandle.getChunk();
        final var archetype = chunk.getArchetype();
        archetype.queueRemoved(chunk, entityHandle.getStorageIndex(), true);
    }

    public EntityHandle createEntity(final Object... components) {
        final var id = this.idCounter.getAndIncrement();

        LOG.debug(LogCategories.ENTITY_LIFECYCLE, "Creating entity with id {}", id);

        final var componentClasses = Arrays.stream(components)
                                           .map(Object::getClass)
                                           .distinct()
                                           .toArray(Class[]::new);
        validateComponents(componentClasses, components);

        // TODO: Change to EntityHandleImpl
        final var entity = new LegacyEntityHandleImpl(id, this);
        final var archetype = findOrCreateArchetype(componentClasses);
        archetype.addEntity(entity, componentClasses, components);

        return entity;
    }

    private void validateComponents(final Class<?>[] componentClasses, final Object[] components) {
        if (componentClasses.length != components.length) {
            final var allComponentClasses = Arrays.stream(components)
                                                  .map(Object::getClass)
                                                  .collect(Collectors.toUnmodifiableList());

            for (final Object component : components) {
                final var clazz = component.getClass();
                if (allComponentClasses.indexOf(clazz) != allComponentClasses.lastIndexOf(clazz)) {
                    throw new IllegalStateException("Multiple components with class \"" + clazz.getSimpleName()
                                                    + "\" supplied to `createEntity()`!");
                }
            }
        }
    }

    public void clear() {
        // XXX: This is not very smart, but effectively achieves a 100% clear of the whole storage
        //      with all of its chunks in any archetype chunk chain. Generates literal shit-ton of
        //      garbage, but as clearing is rare thing to happen, this should not have adverse
        //      effects on performance.
        this.archetypes.clear();
    }
}
