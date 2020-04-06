package fi.jakojaannos.roguelite.engine.ecs;

import java.util.Collection;

/**
 * Logical group of systems with internal ordering. Systems inside a group are executed sequentially.
 */
public interface SystemGroup {
    boolean isEnabled();

    Collection<SystemGroup> getDependencies();

    // TODO: Change to EcsSystem
    Collection<Object> getSystems();

    String getName();

    interface Builder {
        SystemGroup buildGroup();

        /**
         * @deprecated Legacy compatibility, removed once all old systems are converted
         */
        @Deprecated
        Builder withSystem(fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem system);

        Builder withSystem(EcsSystem<?, ?, ?> system);

        Builder dependsOn(SystemGroup... dependencies);
    }
}
