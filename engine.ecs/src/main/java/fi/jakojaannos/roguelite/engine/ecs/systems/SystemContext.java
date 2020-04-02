package fi.jakojaannos.roguelite.engine.ecs.systems;

import java.util.ArrayList;
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
public final class SystemContext {
    private final SystemRequirements requirements;
    private final SystemDependencies dependencies;
    private final ECSSystem instance;
    private final Collection<SystemGroup> groups;

    public SystemRequirements getRequirements() {
        return this.requirements;
    }

    public SystemDependencies getDependencies() {
        return this.dependencies;
    }

    public ECSSystem getInstance() {
        return this.instance;
    }

    public Stream<SystemGroup> getGroups() {
        return this.groups.stream();
    }

    private SystemContext(
            final SystemRequirements requirements,
            final SystemDependencies dependencies,
            final ECSSystem instance,
            final Collection<SystemGroup> groups
    ) {
        this.requirements = requirements;
        this.dependencies = dependencies;
        this.instance = instance;
        this.groups = groups;
    }

    public static final class Builder {
        private final Collection<SystemGroup> groups = new ArrayList<>();
        private SystemRequirements requirements;
        private SystemDependencies dependencies;
        private ECSSystem instance;

        public SystemContext build() {
            return new SystemContext(this.requirements, this.dependencies, this.instance, this.groups);
        }

        public Builder instance(final ECSSystem instance) {
            this.instance = instance;
            return this;
        }

        public Builder requirements(final SystemRequirements requirements) {
            this.requirements = requirements;
            return this;
        }

        public Builder dependencies(final SystemDependencies dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public Builder group(final SystemGroup group) {
            this.groups.add(group);
            return this;
        }
    }
}
