package fi.jakojaannos.roguelite.engine.ecs.legacy;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.world.LegacyEntityHandleImpl;

@Deprecated
public interface Entity {
    int getId();

    @Deprecated
    boolean isMarkedForRemoval();

    @Deprecated
    default EntityHandle asHandle() {
        return (LegacyEntityHandleImpl) this;
    }
}
