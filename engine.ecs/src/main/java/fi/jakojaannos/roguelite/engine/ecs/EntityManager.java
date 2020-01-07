package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.engine.ecs.entities.EntityManagerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Allows manipulating entities and their components. Accessor to a {@link World world's} entity
 * storage. All entity-related data mutations happen through the <code>EntityManager</code>.
 */
public interface EntityManager {
    static EntityManager createNew(int entityCapacity, int maxComponentTypes) {
        return new EntityManagerImpl(entityCapacity, maxComponentTypes);
    }

    /**
     * Registers a new component group. Should be called only before any entities are created.
     *
     * @param group group to be registered.
     */
    void registerComponentGroup(ComponentGroup group);

    /**
     * Creates a new entity. The created entity is added to the game world during the next {@link
     * #applyModifications()}
     *
     * @return the entity created
     */
    Entity createEntity();

    /**
     * Destroys an entity. The entity is marked for removal instantly, and destroyed during the next
     * {@link #applyModifications()}
     *
     * @param entity the entity to mark for removal
     */
    void destroyEntity(Entity entity);

    /**
     * Applies all entity mutations. Executes all tasks queued with {@link #createEntity()} and
     * {@link #destroyEntity(Entity)}
     */
    void applyModifications();

    /**
     * Adds the component to the entity.
     *
     * @param entity       Entity to add the component to
     * @param component    Component to add
     * @param <TComponent> Type of the component
     *
     * @return The added component
     */
    <TComponent extends Component> TComponent addComponentTo(Entity entity, TComponent component);


    /**
     * Removes a component of given type from the entity.
     *
     * @param entity    Entity to remove the component from
     * @param component Component to remove
     */
    default <TComponent extends Component> void removeComponentFrom(
            final Entity entity,
            final TComponent component
    ) {
        removeComponentFrom(entity, component.getClass());
    }

    /**
     * Removes a component of given type from the entity.
     *
     * @param entity         Entity to remove the component from
     * @param componentClass Type of the component to remove
     */
    void removeComponentFrom(Entity entity, Class<? extends Component> componentClass);

    /**
     * Gets the component of given type from the entity.
     *
     * @param entity         Entity to get components from
     * @param componentClass Component class to get
     * @param <TComponent>   Type of the component
     *
     * @return If component exists, component optional of the component. Otherwise, an empty
     * optional
     */
    <TComponent extends Component> Optional<TComponent> getComponentOf(
            Entity entity,
            Class<? extends TComponent> componentClass
    );

    /**
     * Checks whether or not the given entity has the specified component.
     *
     * @param entity         entity to check
     * @param componentClass type of the component to look for
     *
     * @return <code>true</code> if the entity has the component, <code>false</code> otherwise
     */
    boolean hasComponent(Entity entity, Class<? extends Component> componentClass);

    /**
     * Checks whether or not the given entity has any component from the specified component group.
     *
     * @param entity entity to check
     * @param group  component group to check
     *
     * @return <code>true</code> if the entity has any of the components, <code>false</code>
     * otherwise
     */
    boolean hasAnyComponentFromGroup(Entity entity, ComponentGroup group);

    /**
     * Removes all components except the component of given type
     *
     * @param entity        the entity to remove components from
     * @param componentType type of the component not to remove
     */
    void clearComponentsExcept(Entity entity, Class<? extends Component> componentType);

    /**
     * Removes all components except ones in the given list
     *
     * @param entity         the entity to remove components from
     * @param componentTypes types of the components not to remove
     */
    void clearComponentsExcept(
            Entity entity,
            Collection<Class<? extends Component>> componentTypes
    );

    /**
     * Gets all entities with given component.
     *
     * @param componentType component type to look for
     * @param <TComponent>  type of the component to look for
     *
     * @return <code>EntityComponentPair</code>s of all the entities and their respective components
     */
    <TComponent extends Component> Stream<EntityComponentPair<TComponent>> getEntitiesWith(
            Class<? extends TComponent> componentType
    );

    /**
     * Gets all entities with components of all given types.
     *
     * @param componentTypes types of the components to look for
     *
     * @return Stream of entities with all given component types
     */
    Stream<Entity> getEntitiesWith(
            Collection<Class<? extends Component>> componentTypes
    );

    /**
     * Gets all entities which have all components specified in <code>required</code> and none of
     * the components specified in <code>excluded</code>.
     *
     * @param required       required component types
     * @param excluded       excluded component types
     * @param requiredGroups required component groups
     * @param excludedGroups excluded component groups
     *
     * @return Stream of entities matching the given criteria
     */
    Stream<Entity> getEntitiesWith(
            Collection<Class<? extends Component>> required,
            Collection<Class<? extends Component>> excluded,
            Collection<ComponentGroup> requiredGroups,
            Collection<ComponentGroup> excludedGroups
    );

    /**
     * Adds the component to the entity if it does not already have a component of the given type.
     * In other words, ensures the entity has a component of given type.
     *
     * @param entity            Entity to add the component to
     * @param componentSupplier Supplier used to get a component to add
     * @param <TComponent>      Type of the component
     *
     * @return The existing component for the entity or the added if the entity did not have
     * component of given type yet
     */
    default <TComponent extends Component> TComponent addComponentIfAbsent(
            final Entity entity,
            final Class<TComponent> componentClass,
            final Supplier<TComponent> componentSupplier
    ) {
        return this.getComponentOf(entity, componentClass)
                   .orElseGet(() -> addComponentTo(entity, componentSupplier.get()));
    }

    /**
     * Removes the component from the entity if it has a component of given type. In other words,
     * ensures that the entity has no component of the given type.
     *
     * @param entity         Entity to remove the component from
     * @param componentClass Type of the component to remove
     *
     * @return <code>true</code> if the component was removed, <code>false</code> otherwise
     */
    default boolean removeComponentIfPresent(
            final Entity entity,
            final Class<? extends Component> componentClass
    ) {
        if (!hasComponent(entity, componentClass)) {
            return false;
        }

        removeComponentFrom(entity, componentClass);
        return true;
    }

    /**
     * Counts how many entities there are currently present in the world.
     *
     * @return the number of entities present
     */
    int entityCount();

    /**
     * Gets a stream containing ALL of the entities in the world. Heavy performance cost if whole
     * stream is iterated, use sparingly and only when absolutely necessary.
     *
     * @return stream of all the entities in the world
     */
    Stream<Entity> getAllEntities();

    /**
     * Marks all entities for removal.
     */
    default void clearEntities() {
        getAllEntities().forEach(this::destroyEntity);
    }

    @RequiredArgsConstructor
    class EntityComponentPair<TComponent extends Component> {
        @Getter private final Entity entity;
        @Getter private final TComponent component;
    }
}
