package fi.jakojaannos.roguelite.engine.ecs;

import java.util.stream.Stream;

/**
 * One-stop solution to all yur configuring-systems-is-a-PITA needs! Define system dependencies, requirements, etc.
 * conveniently with a single builder instance. Most likely ends up making system configuration even worse, but I have
 * wasted so many hours on this, I DO NOT EVEN CARE ANYMORE!
 *
 * @see ECSSystem#declareRequirements(RequirementsBuilder)
 */
public interface RequirementsBuilder {
    /**
     * Adds a dependency for this system to be ticked after given <code>other</code> system.
     *
     * @param other the other system we want to make sure has ticked before ticking this system
     *
     * @return the builder for chaining
     */
    RequirementsBuilder tickAfter(Class<? extends ECSSystem> other);

    /**
     * Adds a dependency for this system to be ticked before given <code>other</code> system.
     *
     * @param other the other system that must not tick before this system
     *
     * @return the builder for chaining
     */
    RequirementsBuilder tickBefore(Class<? extends ECSSystem> other);

    /**
     * Adds a dependency for this system to be ticked after given system group.
     *
     * @param group the systems to ensure to be ticked before ticking this system
     *
     * @return the builder for chaining
     */
    RequirementsBuilder tickAfter(SystemGroup group);

    /**
     * Adds a dependency for this system to be ticked before given system group.
     *
     * @param group the system group that must not tick before this system
     *
     * @return the builder for chaining
     */
    RequirementsBuilder tickBefore(SystemGroup group);

    /**
     * Adds this system to a system group. System groups are used for easier dependency definition.
     *
     * @param group group to add this system to
     *
     * @return the builder for chaining
     */
    RequirementsBuilder addToGroup(SystemGroup group);

    /**
     * Marks requirement for all entities this system handles to have a component of given type.
     *
     * @param componentClass type of the required component
     *
     * @return the builder for chaining
     */
    RequirementsBuilder withComponent(Class<? extends Component> componentClass);

    /**
     * Marks requirement for all entities this system handles to not have a component of given type. This means that any
     * entities with given component type are excluded from being passed into {@link ECSSystem#tick(Stream, World)} for
     * the system.
     *
     * @param componentClass type of the excluded component
     *
     * @return the builder for chaining
     */
    RequirementsBuilder withoutComponent(Class<? extends Component> componentClass);

    /**
     * Marks requirement for a resource to be available for this system to be able to tick.
     *
     * @param resource type of the required resource
     *
     * @return the builder for chaining
     */
    RequirementsBuilder requireResource(Class<? extends Resource> resource);

    /**
     * Marks requirement for a provided resource to be available for this system to be able to tick.
     *
     * @param resource type of the required resource
     *
     * @return the builder for chaining
     */
    RequirementsBuilder requireProvidedResource(Class<? extends ProvidedResource> resource);
}
