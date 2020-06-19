package fi.jakojaannos.riista.ecs.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import javax.annotation.Nullable;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.SystemGroup;

public class SystemGroupImpl implements SystemGroup {
    private static final Logger LOG = LoggerFactory.getLogger(SystemGroupImpl.class);

    private final Collection<SystemGroup> dependencies;
    private final Collection<EcsSystem<?, ?, ?>> systems;
    private final String name;
    private final boolean enabledByDefault;
    private final int id;

    @Override
    public Collection<SystemGroup> getDependencies() {
        return this.dependencies;
    }

    @Override
    public Collection<EcsSystem<?, ?, ?>> getSystems() {
        return this.systems;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isEnabledByDefault() {
        return this.enabledByDefault;
    }

    private SystemGroupImpl(
            final Collection<SystemGroup> dependencies,
            final Collection<EcsSystem<?, ?, ?>> systems,
            final String name,
            final boolean enabledByDefault,
            final int id
    ) {
        this.dependencies = dependencies;
        this.systems = systems;
        this.name = name;
        this.enabledByDefault = enabledByDefault;
        this.id = id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof SystemGroupImpl that) {
            return this.id == that.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    public static final class Builder implements SystemGroup.Builder {
        private final Collection<SystemGroup> dependencies = new ArrayList<>();
        private final Collection<EcsSystem<?, ?, ?>> systems = new ArrayList<>();
        private final String name;
        private final int id;
        private boolean enabledByDefault;
        @Nullable private SystemGroupImpl built;

        public Builder(final String name, final int id) {
            this.name = name;
            this.id = id;

            this.enabledByDefault = true;
        }

        @Override
        public SystemGroup buildGroup() {
            if (this.built == null) {
                this.built = new SystemGroupImpl(this.dependencies,
                                                 this.systems,
                                                 this.name,
                                                 this.enabledByDefault,
                                                 this.id);
            } else {
                LOG.warn("buildGroup() called multiple times! Offending group: \"{}\"", this.name);
            }
            return this.built;
        }

        @Override
        public SystemGroup.Builder withSystem(final EcsSystem<?, ?, ?> system) {
            this.systems.add(system);
            return this;
        }

        @Override
        public SystemGroup.Builder disabledByDefault() {
            this.enabledByDefault = false;
            return this;
        }

        @Override
        public SystemGroup.Builder dependsOn(final SystemGroup... dependencies) {
            this.dependencies.addAll(Arrays.asList(dependencies));
            return this;
        }

        public SystemGroup asGroup() {
            if (this.built == null) {
                LOG.warn("buildGroup() not called for a group before the dispatcher was built! "
                         + "Offending group: \"{}\"", this.name);
                buildGroup();
            }

            return this.built;
        }
    }
}
