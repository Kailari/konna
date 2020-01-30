package fi.jakojaannos.roguelite.engine.ecs.systems;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Collection;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;

/**
 * Everything known about a system. More specifically everything that ever needs to be known from the {@link
 * SystemDispatcher dispatcher} point of view, at least. Analogous to {@link InternalSystemGroup}, but for single
 * systems.
 *
 * @see InternalSystemGroup
 */
@Builder(builderClassName = "Builder")
public final class SystemContext {
    @Getter private final SystemRequirements requirements;
    @Getter private final SystemDependencies dependencies;
    @Getter private final ECSSystem instance;

    @Singular private final Collection<SystemGroup> groups;

    public Stream<SystemGroup> getGroups() {
        return this.groups.stream();
    }
}
