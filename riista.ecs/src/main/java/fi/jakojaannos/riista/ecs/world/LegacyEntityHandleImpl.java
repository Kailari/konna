package fi.jakojaannos.riista.ecs.world;

import fi.jakojaannos.riista.ecs.legacy.Entity;
import fi.jakojaannos.riista.ecs.world.storage.EntityStorage;

@Deprecated
public class LegacyEntityHandleImpl extends EntityHandleImpl implements Entity {
    @Override
    public boolean isMarkedForRemoval() {
        return this.isPendingRemoval();
    }

    public LegacyEntityHandleImpl(
            final int id,
            final EntityStorage storage
    ) {
        super(id, storage);
    }

    @Override
    public String toString() {
        return "LegacyEntityHandle[" + this.getId() + "]";
    }
}
