package fi.jakojaannos.riista.ecs.legacy;

import fi.jakojaannos.riista.ecs.EntityHandle;
import fi.jakojaannos.riista.ecs.world.LegacyEntityHandleImpl;

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
