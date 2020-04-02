package fi.jakojaannos.roguelite.engine.ecs.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;

/**
 * Default {@link RequirementsBuilder} implementation. Handles delegating system/group/component relations and/or
 * requirements to other internal classes.
 */
public class RequirementsBuilderImpl implements RequirementsBuilder {
    private final SystemRequirements.Builder requirements = SystemRequirements.builder();
    private final SystemDependencyResolver dependencyBuilder;
    private final ECSSystem instance;
    private final SystemContext.Builder builder = new SystemContext.Builder();

    public RequirementsBuilderImpl(
            final SystemDependencyResolver dependencyBuilder,
            final ECSSystem instance
    ) {
        this.dependencyBuilder = dependencyBuilder;
        this.instance = instance;
    }

    public SystemContext build() {
        return this.builder.instance(this.instance)
                           .requirements(this.requirements.build())
                           .dependencies(this.dependencyBuilder.buildFor(this.instance))
                           .build();
    }

    @Override
    public RequirementsBuilder tickAfter(final Class<? extends ECSSystem> other) {
        this.dependencyBuilder.tickAfter(this.instance, other);
        return this;
    }

    @Override
    public RequirementsBuilder tickBefore(final Class<? extends ECSSystem> other) {
        this.dependencyBuilder.tickBefore(this.instance, other);
        return this;
    }

    @Override
    public RequirementsBuilder tickAfter(final SystemGroup group) {
        this.dependencyBuilder.tickAfter(this.instance, group);
        return this;
    }

    @Override
    public RequirementsBuilder tickBefore(final SystemGroup group) {
        this.dependencyBuilder.tickBefore(this.instance, group);
        return this;
    }

    @Override
    public RequirementsBuilder addToGroup(final SystemGroup group) {
        this.dependencyBuilder.addToGroup(group, this.instance);
        this.builder.group(group);
        return this;
    }

    @Override
    public RequirementsBuilder withComponent(final Class<? extends Component> componentClass) {
        this.requirements.requiredComponent(componentClass);
        return this;
    }

    @Override
    public RequirementsBuilder withoutComponent(final Class<? extends Component> componentClass) {
        this.requirements.excludedComponent(componentClass);
        return this;
    }

    @Override
    public RequirementsBuilder withComponentFrom(final ComponentGroup componentGroup) {
        this.requirements.requiredGroup(componentGroup);
        return this;
    }

    @Override
    public RequirementsBuilder withoutComponentsFrom(final ComponentGroup componentGroup) {
        this.requirements.excludedGroup(componentGroup);
        return this;
    }

    @Override
    public RequirementsBuilder requireResource(final Class<? extends Resource> resource) {
        this.requirements.requiredResource(resource);
        return this;
    }

    @Override
    public RequirementsBuilder requireProvidedResource(final Class<? extends ProvidedResource> resource) {
        this.requirements.requiredProvidedResource(resource);
        return this;
    }
}
