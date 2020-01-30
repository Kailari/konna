package fi.jakojaannos.roguelite.engine.ecs.systems;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Collection;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;

/**
 * Internal storage for a {@link SystemGroup}. Analogous to {@link SystemContext} but for groups.
 *
 * @see SystemContext
 */
@Builder(builderClassName = "Builder")
public class InternalSystemGroup {
    @Getter private final SystemGroup group;
    @Singular private final Collection<ECSSystem> systems;
    @Singular private final Collection<Class<? extends ECSSystem>> dependencies;
    @Singular private final Collection<SystemGroup> groupDependencies;

    public Stream<Class<? extends ECSSystem>> getSystems() {
        return this.systems.stream()
                           .map(ECSSystem::getClass);
    }

    public Stream<Class<? extends ECSSystem>> getDependencies() {
        return this.dependencies.stream();
    }

    public Stream<SystemGroup> getGroupDependencies() {
        return this.groupDependencies.stream();
    }
}
