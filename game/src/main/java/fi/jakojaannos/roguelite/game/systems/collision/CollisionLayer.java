package fi.jakojaannos.roguelite.game.systems.collision;

import lombok.extern.slf4j.Slf4j;

import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;

@Slf4j
public enum CollisionLayer {
    NONE,
    COLLIDE_ALL,
    OVERLAP_ALL,
    OBSTACLE,
    PLAYER,
    PLAYER_PROJECTILE,
    ENEMY;

    private static final int MASK_SIZE = 1;

    static {
        LOG.debug("Registering collision layers...");

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

    public boolean isSolidTo(final CollisionLayer other) {
        return other != NONE && BitMaskUtils.isNthBitSet(other.collisionMask, getIndex());
    }

    public boolean canOverlapWith(final CollisionLayer other) {
        return other != NONE && BitMaskUtils.isNthBitSet(other.overlapMask, getIndex());
    }

    private int getIndex() {
        return ordinal() - 1;
    }

    private void setCollidesWith(final CollisionLayer... layers) {
        LOG.trace("Setting collisions for {}", this.name());
        for (final var other : layers) {
            LOG.trace("\t-> {}", other.name());
            BitMaskUtils.setNthBit(this.collisionMask, other.getIndex());
        }
    }

    private void setOverlapsWith(final CollisionLayer... layers) {
        LOG.trace("Setting overlaps for {}", this.name());
        for (final var other : layers) {
            LOG.trace("\t-> {}", other.name());
            BitMaskUtils.setNthBit(this.overlapMask, other.getIndex());
        }
    }
}
