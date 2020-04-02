package fi.jakojaannos.roguelite.engine.ecs.entities;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;

public final class EntityImpl implements Entity {
    private final int id;
    private final byte[] componentBitmask;
    private boolean markedForRemoval;

    @Override
    public int getId() {
        return this.id;
    }

    public byte[] getComponentBitmask() {
        return this.componentBitmask;
    }

    @Override
    public boolean isMarkedForRemoval() {
        return this.markedForRemoval;
    }

    public EntityImpl(final int id, final int maxComponentTypes) {
        this.id = id;
        this.markedForRemoval = false;

        final int nBytes = BitMaskUtils.calculateMaskSize(maxComponentTypes);
        this.componentBitmask = new byte[nBytes];
    }

    void markForRemoval() {
        this.markedForRemoval = true;
    }
}
