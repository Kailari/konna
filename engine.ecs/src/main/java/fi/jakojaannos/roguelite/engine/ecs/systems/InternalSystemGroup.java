package fi.jakojaannos.roguelite.engine.ecs.systems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;

/**
 * Internal storage for a {@link SystemGroup}. Analogous to {@link SystemContext} but for groups.
 *
 * @see SystemContext
 */
public class InternalSystemGroup {
    private final SystemGroup group;
    private final Collection<ECSSystem> systems;
    private final Collection<Class<? extends ECSSystem>> dependencies;
    private final Collection<SystemGroup> groupDependencies;

    public static Builder builder() {
        return new Builder();
    }

    public SystemGroup getGroup() {
        return this.group;
    }

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

    private InternalSystemGroup(
            final SystemGroup group,
            final Collection<ECSSystem> systems,
            final Collection<Class<? extends ECSSystem>> dependencies,
            final Collection<SystemGroup> groupDependencies
    ) {
        this.group = group;
        this.systems = systems;
        this.dependencies = dependencies;
        this.groupDependencies = groupDependencies;
    }

    public static final class Builder {
        private final Collection<SystemGroup> groupDependencies = new ArrayList<>();
        private final Collection<Class<? extends ECSSystem>> dependencies = new ArrayList<>();
        private final Collection<ECSSystem> systems = new ArrayList<>();
        private SystemGroup group;

        public InternalSystemGroup build() {
            return new InternalSystemGroup(this.group, this.systems, this.dependencies, this.groupDependencies);
        }

        public Builder group(final SystemGroup group) {
            this.group = group;
            return this;
        }

        public void dependency(final Class<? extends ECSSystem> dependency) {
            this.dependencies.add(dependency);
        }

        public void system(final ECSSystem instance) {
            this.systems.add(instance);
        }

        public void groupDependency(final SystemGroup dependency) {
            this.groupDependencies.add(dependency);
        }
    }
}
