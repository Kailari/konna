package fi.jakojaannos.roguelite.game.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.jakojaannos.riista.utilities.BitMaskUtils;
import fi.jakojaannos.roguelite.game.LogCategories;

public enum CollisionLayer {
    NONE,
    COLLIDE_ALL,
    OVERLAP_ALL,
    OBSTACLE,
    PLAYER,
    PLAYER_PROJECTILE,
    ENEMY;

    private static final Logger LOG = LoggerFactory.getLogger(CollisionLayer.class);

    private static final int MASK_SIZE = 1;

    static {
        LOG.debug(LogCategories.COLLISION_LAYER, "Registering collision layers...");

        COLLIDE_ALL.setCollidesWith(values());
        OVERLAP_ALL.setCollidesWith(values());
        PLAYER.setCollidesWith(COLLIDE_ALL, OBSTACLE);
        PLAYER.setOverlapsWith(OVERLAP_ALL, ENEMY);
        PLAYER_PROJECTILE.setCollidesWith(COLLIDE_ALL, OBSTACLE);
        PLAYER_PROJECTILE.setOverlapsWith(OVERLAP_ALL, ENEMY);
        ENEMY.setCollidesWith(COLLIDE_ALL, OBSTACLE);
        ENEMY.setOverlapsWith(OVERLAP_ALL, PLAYER);
    }

    private final byte[] collisionMask = new byte[MASK_SIZE];
    private final byte[] overlapMask = new byte[MASK_SIZE];

    private int getIndex() {
        return ordinal() - 1;
    }

    public boolean isSolidTo(final CollisionLayer other) {
        return other != NONE && BitMaskUtils.isNthBitSet(other.collisionMask, getIndex());
    }

    public boolean canOverlapWith(final CollisionLayer other) {
        return other != NONE && BitMaskUtils.isNthBitSet(other.overlapMask, getIndex());
    }

    private void setCollidesWith(final CollisionLayer... layers) {
        LOG.trace(LogCategories.COLLISION_LAYER, "Collisions for {}", this.name());
        for (final var other : layers) {
            LOG.trace(LogCategories.COLLISION_LAYER, "\t-> {}", other.name());
            BitMaskUtils.setNthBit(this.collisionMask, other.getIndex());
        }
    }

    private void setOverlapsWith(final CollisionLayer... layers) {
        LOG.trace(LogCategories.COLLISION_LAYER, "Overlaps for {}", this.name());
        for (final var other : layers) {
            LOG.trace(LogCategories.COLLISION_LAYER, "\t-> {}", other.name());
            BitMaskUtils.setNthBit(this.overlapMask, other.getIndex());
        }
    }
}
