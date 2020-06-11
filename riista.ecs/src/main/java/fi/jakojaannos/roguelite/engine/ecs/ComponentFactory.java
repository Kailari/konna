package fi.jakojaannos.roguelite.engine.ecs;

/**
 * Allows constructing components which require a reference to the entity being created. Note that the entity is not
 * guaranteed to have any components when the factory is executed. To make myself clear, <strong>NEVER ASSUME THE ENTITY
 * TO HAVE ANY COMPONENTS WHILE IN THE FACTORY</strong>
 *
 * @param <TComponent> the type of the produced component
 */
public interface ComponentFactory<TComponent> {
    /**
     * Convenience utility for tricking the compiler for nicer syntax when defining archetypes programmatically.
     * Normally, one would have to explicitly cast the component factory lambda into a component factory (as they are
     * passed to the {@link fi.jakojaannos.roguelite.engine.ecs.data.resources.Entities#createEntity(Object...)
     * createEntity(...)} as <code>Object...</code>) in order to tell the compiler the type. This utility allows the
     * compiler to make that cast implicitly, resulting in bit more concise archetype definitions.
     * <p>
     * To give a more concrete example, without the method:
     * <pre>{@code
     * entities.createEntity(new SomeComponent(),
     *                       new AnotherComponent(),
     *                       // Note the lengthy class
     *                       (ComponentFactory<ThirdComponent>) entity -> new ThirdComponent(entity, arg1, arg2, etc));
     * }</pre>
     * <p>
     * and with the method:
     * <pre>{@code
     * import static fi.jakojaannos.roguelite.engine.ecs.ComponentFactory.factory;
     *
     * // ...
     *
     * entities.createEntity(new SomeComponent(),
     *                       new AnotherComponent(),
     *                       // Behold! The cast is gone
     *                       factory(entity -> new ThirdComponent(entity, arg1, arg2, etc)));
     * }</pre>
     * <p>
     * The difference is arguably subtle and requires a static import, but this can be used to make the lines a bit
     * shorter.
     *
     * @param factory      the factory to output
     * @param <TComponent> the type of the produced component
     *
     * @return the exact same factory as given in the <code>factory</code> parameter
     */
    static <TComponent> ComponentFactory<TComponent> factory(final ComponentFactory<TComponent> factory) {
        return factory;
    }

    TComponent construct(EntityHandle entity);
}
