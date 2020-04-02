package fi.jakojaannos.roguelite.engine.ecs.systems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.*;

/**
 * Runtime requirements of a system. These are resources the system consumes by reading/writing/mutating over and can
 * assume to be available when executing its {@link fi.jakojaannos.roguelite.engine.ecs.ECSSystem#tick(Stream, World)}
 * tick} function.
 */
record SystemRequirements(
        Collection<Class<? extends Component>>requiredComponents,
        Collection<Class<? extends Component>>excludedComponents,
        Collection<ComponentGroup>requiredGroups,
        Collection<ComponentGroup>excludedGroups,
        Collection<Class<? extends Resource>>requiredResources,
        Collection<Class<? extends ProvidedResource>>requiredProvidedResources
) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Collection<Class<? extends Component>> requiredComponents = new ArrayList<>();
        private final Collection<Class<? extends Component>> excludedComponents = new ArrayList<>();
        private final Collection<ComponentGroup> requiredGroups = new ArrayList<>();
        private final Collection<ComponentGroup> excludedGroups = new ArrayList<>();
        private final Collection<Class<? extends Resource>> requiredResources = new ArrayList<>();
        private final Collection<Class<? extends ProvidedResource>> requiredProvidedResources = new ArrayList<>();

        public SystemRequirements build() {
            return new SystemRequirements(this.requiredComponents,
                                          this.excludedComponents,
                                          this.requiredGroups,
                                          this.excludedGroups,
                                          this.requiredResources,
                                          this.requiredProvidedResources);
        }

        public void requiredComponent(final Class<? extends Component> componentClass) {
            this.requiredComponents.add(componentClass);
        }

        public void excludedComponent(final Class<? extends Component> componentClass) {
            this.excludedComponents.add(componentClass);
        }

        public void requiredGroup(final ComponentGroup componentGroup) {
            this.requiredGroups.add(componentGroup);
        }

        public void excludedGroup(final ComponentGroup componentGroup) {
            this.excludedGroups.add(componentGroup);
        }

        public void requiredResource(final Class<? extends Resource> resource) {
            this.requiredResources.add(resource);
        }

        public void requiredProvidedResource(final Class<? extends ProvidedResource> resource) {
            this.requiredProvidedResources.add(resource);
        }
    }
}
