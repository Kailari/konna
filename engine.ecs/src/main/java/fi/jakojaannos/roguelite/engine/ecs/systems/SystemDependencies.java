package fi.jakojaannos.roguelite.engine.ecs.systems;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;

/**
 * Runtime dependencies of a system. These are pre-defined execution-flow -limiting factors that need to be accounted
 * for when determining system execution order.
 */
@RequiredArgsConstructor
class SystemDependencies {
    private final Collection<Class<? extends ECSSystem>> dependencies;
    private final Collection<SystemGroup> groupDependencies;

    public Stream<Class<? extends ECSSystem>> stream() {
        return this.dependencies.stream();
    }

    public Stream<SystemGroup> groupDependenciesAsStream() {
        return this.groupDependencies.stream();
    }
}
