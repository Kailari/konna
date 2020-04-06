package fi.jakojaannos.roguelite.engine.ecs.newecs.world;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.newecs.World;

@Deprecated
public class LegacyEntityHandleImpl extends EntityHandleImpl implements Entity {
    @Override
    public boolean isMarkedForRemoval() {
        return this.isPendingRemoval();
    }

    public LegacyEntityHandleImpl(
            final int id,
            final World world
    ) {
        super(id, world);
    }
}
