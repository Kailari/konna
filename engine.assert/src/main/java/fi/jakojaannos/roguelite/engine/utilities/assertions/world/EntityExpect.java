package fi.jakojaannos.roguelite.engine.utilities.assertions.world;

/**
 * Perform expectation assertions over state of an entity and its components.
 */
public interface EntityExpect {
    /**
     * Asserts that an entity has a component of given class. Additionally, if the assertion passes, creates a component
     * matcher for performing further assertions over that component, if desired.
     * <p>
     * The assertion fails if the component does not exist.
     *
     * @param componentClass class of the component to expect to exist
     * @param <TComponent>   type of the component
     *
     * @return Component matcher for asserting over the state of the component
     *
     * @see ComponentMatcher
     */
    <TComponent> ComponentMatcher<TComponent> toHaveComponent(Class<TComponent> componentClass);

    /**
     * Asserts that the entity does not have a component of the given class.
     * <p>
     * The assertion fails if the component exists.
     *
     * @param componentClass class of the unexpected component
     */
    void toNotHaveComponent(Class<?> componentClass);
}
