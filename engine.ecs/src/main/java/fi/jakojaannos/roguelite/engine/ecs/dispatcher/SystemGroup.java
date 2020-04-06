package fi.jakojaannos.roguelite.engine.ecs.dispatcher;

import java.util.Collection;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.newecs.EcsSystem;

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
        Builder withSystem(ECSSystem system);

        Builder withSystem(EcsSystem<?, ?, ?> system);

        Builder dependsOn(SystemGroup... dependencies);
    }
}
