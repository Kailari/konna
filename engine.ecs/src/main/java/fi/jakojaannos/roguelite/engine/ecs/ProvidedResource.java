package fi.jakojaannos.roguelite.engine.ecs;

/**
 * Special type of {@link Resource} which cannot be created on-the-fly. These resources are instead provided by some
 * specific subsystem. E.g. networking subsystem provides the <code>Network</code> resource, engine provides the
 * <code>GameStateManager</code>, etc.
 *
 * @see Resource
 */
@Deprecated
public interface ProvidedResource {
}
