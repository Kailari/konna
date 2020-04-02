package fi.jakojaannos.roguelite.engine.ecs.systems;

import java.util.Collection;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;

/**
 * Runtime dependencies of a system. These are pre-defined execution-flow -limiting factors that need to be accounted
 * for when determining system execution order.
 */
class SystemDependencies {
    private final Collection<Class<? extends ECSSystem>> dependencies;
    private final Collection<SystemGroup> groupDependencies;

    SystemDependencies(
            final Collection<Class<? extends ECSSystem>> dependencies,
            final Collection<SystemGroup> groupDependencies
    ) {
        this.dependencies = dependencies;
        this.groupDependencies = groupDependencies;
    }

    Stream<Class<? extends ECSSystem>> stream() {
        return this.dependencies.stream();
    }

    Stream<SystemGroup> groupDependenciesAsStream() {
        return this.groupDependencies.stream();
    }
}
