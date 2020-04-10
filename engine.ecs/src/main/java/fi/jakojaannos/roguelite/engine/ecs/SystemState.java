package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;

public interface SystemState {
    @Deprecated
    default boolean isEnabled(final ECSSystem system) {
        return isEnabled((Object) system);
    }

    default boolean isEnabled(final EcsSystem<?, ?, ?> system) {
        return isEnabled((Object) system);
    }

    boolean isEnabled(Object system);

    boolean isEnabled(SystemGroup systemGroup);

    void setState(Object system, boolean state);

    void setState(SystemGroup systemGroup, boolean state);
}
