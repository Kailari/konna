package fi.jakojaannos.roguelite.engine.ecs.entities;

import lombok.Getter;

import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;

public class EntityImpl implements fi.jakojaannos.roguelite.engine.ecs.Entity {
    @Getter private final int id;
    @Getter private final byte[] componentBitmask;
    @Getter private boolean markedForRemoval;

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
