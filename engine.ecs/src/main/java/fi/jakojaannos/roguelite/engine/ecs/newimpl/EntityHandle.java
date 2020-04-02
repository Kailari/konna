package fi.jakojaannos.roguelite.engine.ecs.newimpl;

public interface EntityHandle {
    /**
     * Tries to add a component to the entity. If entity already has a component of the specified type, the method does
     * nothing.
     *
     * @param component    component to be added
     * @param <TComponent> type of the component to add
     *
     * @return <code>true</code> if the component was added. <code>false</code> if the entity already had a component
     *         of the given type
     */
    <TComponent> boolean addComponent(TComponent component);

    /**
     * Tries to remove a component of the given type from the entity. If the entity does not have a component with
     * specified type, the method does nothing.
     *
     * @param componentClass type of the component to remove
     * @param <TComponent>   type of the component to remove
     *
     * @return <code>true</code> if the component was removed. <code>false</code> if the entity did not have a
     *         component of the given type
     */
    <TComponent> boolean removeComponent(Class<TComponent> componentClass);

    /**
     * Checks if the entity has a component with the given type.
     *
     * @param componentClass type of the component
     * @param <TComponent>   type of the component
     *
     * @return <code>true</code> if the entity has the component, otherwise <code>false</code>.
     */
    <TComponent> boolean hasComponent(Class<TComponent> componentClass);

    /**
     * Destroys this entity. Entities are not immediately destroyed, rather they are marked for removal after the
     * current tick, meaning that all systems will still be ticked for that entity for the remainder of this tick.
     * <p>
     * In other words, <strong>LEAVING THE ENTITY IN INVALID STATE AFTER DESTROYING IT IS NOT ALLOWED</strong>, the
     * entity might still be used elsewhere for the remainder of this tick and having it in an invalid state might cause
     * hard to catch bugs and odd behavior in edge-cases.
     */
    void destroy();
}
