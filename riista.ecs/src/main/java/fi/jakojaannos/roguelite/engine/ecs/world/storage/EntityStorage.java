package fi.jakojaannos.roguelite.engine.ecs.world.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.ComponentFactory;
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
        // Filter by available components and flatten per-archetype streams to a single stream
        // FIXME: Figure out how to avoid having to take immutable copy for iteration
        //  - custom list collection with support for spliterator might be the easiest solution
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
                         .flatMap(archetype -> archetype.stream(componentClasses,
                                                                optional,
                                                                excluded,
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
     * @param entity         the entity to add the component to
     * @param componentClass class of the added component
     * @param component      the component to add
     * @param <TComponent>   type of the added component
     *
     * @return <code>true</code> if the component was added
     */
    // FIXME: Try to get rid of synchronized on these
    public synchronized <TComponent> boolean addComponent(
            final EntityHandleImpl entity,
            final Class<TComponent> componentClass,
            final TComponent component
    ) {
        // Check if we need to do something
        final var chunk = entity.getChunk();
        final var archetype = chunk.getArchetype();
        if (archetype.hasComponent(componentClass)) {
            // Entity already has the component, do nothing and return false
            return false;
        }

        // Find or create the new archetype
        final var oldComponents = archetype.getComponentClasses();
        final var componentClassesAsList = new ArrayList<>(List.of(oldComponents));
        componentClassesAsList.add(componentClass);
        final var componentClasses = componentClassesAsList.toArray(Class[]::new);

        final var newArchetype = findOrCreateArchetype(componentClasses);

        // Fetch components
        final var storages = chunk.fetchStorages(oldComponents,
                                                 new boolean[oldComponents.length],
                                                 new boolean[oldComponents.length]);
        final var components = new Object[componentClasses.length];
        for (int i = 0; i < oldComponents.length; i++) {
            components[i] = storages[i][entity.getStorageIndex()];
        }
        components[components.length - 1] = component;

        // Move to the new archetype
        archetype.queueRemoved(chunk, entity.getStorageIndex(), false);
        newArchetype.addEntity(entity, componentClasses, components);

        // The component was added, return true
        return true;
    }

    public synchronized <TComponent> boolean removeComponent(
            final EntityHandleImpl entity,
            final Class<TComponent> componentClass
    ) {
        // Check if we need to do something
        final var chunk = entity.getChunk();
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

        // Fetch preserved components
        final var storages = chunk.fetchStorages(componentClasses,
                                                 new boolean[componentClasses.length],
                                                 new boolean[componentClasses.length]);
        final var components = new Object[componentClasses.length];
        for (int i = 0; i < components.length; i++) {
            components[i] = storages[i][entity.getStorageIndex()];
        }

        // Move to the new archetype
        archetype.queueRemoved(chunk, entity.getStorageIndex(), false);
        newArchetype.addEntity(entity, componentClasses, components);

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

        // TODO: Change to EntityHandleImpl once legacy is finally booted
        final var entity = new LegacyEntityHandleImpl(id, this);

        // Some of the given components may actually be factories. Execute those factories and
        // create an array with the factories replaced by their end product components
        final var actualComponents = Stream.concat(Arrays.stream(components)
                                                         .filter(Predicate.not(EntityStorage::isComponentFactory)),
                                                   Arrays.stream(components)
                                                         .filter(EntityStorage::isComponentFactory)
                                                         .map(ComponentFactory.class::cast)
                                                         .map(factory -> factory.construct(entity)))
                                           .toArray();

        // Figure out what the component classes are. These are used to validate that the given set of components is
        // valid (e.g. no duplicate components) and re-used later to select the archetype for the entity
        final var componentClasses = Arrays.stream(actualComponents)
                                           .map(Object::getClass)
                                           .distinct()
                                           .toArray(Class[]::new);
        validateComponents(componentClasses, actualComponents);

        final var archetype = findOrCreateArchetype(componentClasses);
        archetype.addEntity(entity, componentClasses, actualComponents);

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

    private static boolean isComponentFactory(final Object componentOrFactory) {
        return ComponentFactory.class.isAssignableFrom(componentOrFactory.getClass());
    }
}
