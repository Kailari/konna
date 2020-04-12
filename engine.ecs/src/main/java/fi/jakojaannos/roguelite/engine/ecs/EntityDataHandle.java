package fi.jakojaannos.roguelite.engine.ecs;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Utility entity/data -handle which wraps an entity handle with some pre-fetched components. An utility for
 * conveniently iterating over entities with their components pre-fetched. Note that regardless of the name,
 * <strong>this is actually not an <code>EntityHandle</code></strong> but merely a convenience wrapper.
 * <p>
 * If a concrete {@link EntityHandle} is required, use the provided {@link #getHandle()} method.
 *
 * @param <TEntityData> type of the component data to pre-fetch
 */
public interface EntityDataHandle<TEntityData> {
    TEntityData getData();

    /**
     * @see EntityHandle#isPendingRemoval()
     */
    boolean isPendingRemoval();

    /**
     * Gets the wrapped concrete entity handle.
     *
     * @return handle to the underlying entity
     */
    EntityHandle getHandle();

    /**
     * @see EntityHandle#getId()
     */
    int getId();

    /**
     * @see EntityHandle#addComponent(Object)
     */
    <TComponent> boolean addComponent(TComponent component);

    /**
     * @see EntityHandle#addOrGet(Class, Supplier)
     */
    <TComponent> TComponent addOrGet(Class<TComponent> componentClass, Supplier<TComponent> component);

    /**
     * @see EntityHandle#removeComponent(Class)
     */
    <TComponent> boolean removeComponent(Class<TComponent> componentClass);

    /**
     * @see EntityHandle#hasComponent(Class)
     */
    <TComponent> boolean hasComponent(Class<TComponent> componentClass);

    /**
     * @see EntityHandle#getComponent(Class)
     */
    <TComponent> Optional<TComponent> getComponent(Class<TComponent> componentClass);

    /**
     * @see EntityHandle#destroy()
     */
    void destroy();
}
