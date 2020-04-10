package fi.jakojaannos.roguelite.engine.ecs.world;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;

@Deprecated
public class LegacyEntityHandleImpl extends EntityHandleImpl implements Entity {
    @Override
    public boolean isMarkedForRemoval() {
        return this.isPendingRemoval();
    }

    public LegacyEntityHandleImpl(
            final int id,
            final WorldImpl world
    ) {
        super(id, world);
    }

    @Override
    public String toString() {
        return "LegacyEntityHandle[" + this.getId() + "]";
    }
}
