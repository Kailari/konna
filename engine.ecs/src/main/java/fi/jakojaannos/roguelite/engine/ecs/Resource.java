package fi.jakojaannos.roguelite.engine.ecs;

/**
 * A shared resource. Scope of resources is per-{@link World world}, so that each world has their
 * own resource instances. This means that all systems during a {@link
 * SystemDispatcher#dispatch(World, double) dispatch} share the same resource instances.
 * <p>
 * As resources are single-instance per world, there is mostly no restrictions on what type of data
 * they should to hold.
 *
 * @see World#getResource(Class)
 * @see RequirementsBuilder#requireResource(Class)
 */
public interface Resource {
}
