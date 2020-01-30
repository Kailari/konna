package fi.jakojaannos.roguelite.engine.ecs.systems;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fi.jakojaannos.roguelite.engine.ecs.DispatcherBuilder;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;

/**
 * Default implementation for {@link DispatcherBuilder}. Gathers {@link RequirementsBuilderImpl requirements builders}
 * and delegates dependency resolving to {@link SystemDependencyResolver}.
 */
public class DispatcherBuilderImpl implements DispatcherBuilder {
    private final List<RequirementsBuilderImpl> systems = new ArrayList<>();
    private final SystemDependencyResolver dependencyResolver = new SystemDependencyResolver();

    @Override
    public DispatcherBuilder withGroup(final SystemGroup group) {
        this.dependencyResolver.addGroup(group);
        return this;
    }

    @Override
    public DispatcherBuilder addGroupDependency(
            final SystemGroup group,
            final SystemGroup dependency
    ) {
        if (group.equals(dependency)) {
            throw new IllegalStateException("Group cannot depend on itself!");
        }

        this.dependencyResolver.addGroupDependency(group, dependency);
        return this;
    }

    @Override
    public DispatcherBuilder withSystem(final ECSSystem system) {
        final var requirementsBuilder = new RequirementsBuilderImpl(this.dependencyResolver, system);
        this.systems.add(requirementsBuilder);
        system.declareRequirements(requirementsBuilder);
        this.dependencyResolver.addInstance(system);
        return this;
    }

    @Override
    public SystemDispatcher build() {
        return new SystemDispatcherImpl(
                new SystemStorage(this.systems.stream()
                                              .map(RequirementsBuilderImpl::build)
                                              .collect(Collectors.toList()),
                                  this.dependencyResolver.buildGroups()));
    }
}
