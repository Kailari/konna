package fi.jakojaannos.roguelite.engine.ecs;

/**
 * Tag-interface for defining regular components. Components are data, that's really about it. Only getters are allowed.
 * Few exceptions to this rule won't be the end of the world, but in general, one should avoid putting any logic to
 * components. In other words: Mutating data is Systems' responsibility, components are just for storing the data.
 * <p>
 * Components are stored in bulk blocks of memory. (Well I just lied, not just yet. It's planned, but implementation is
 * not quite there yet), meaning that components should be as compact as possible. Storing arrays or any variable sized
 * data-types in components should be avoided, and might be prohibited in near future.
 */
@Deprecated
public interface Component {
}
