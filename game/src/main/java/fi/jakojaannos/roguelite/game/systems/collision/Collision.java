package fi.jakojaannos.roguelite.game.systems.collision;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;

public abstract class Collision {
    private final Type type;
    private final Mode mode;

    public Type getType() {
        return this.type;
    }

    public Mode getMode() {
        return this.mode;
    }

    public final boolean isEntity() {
        return this.type == Type.ENTITY;
    }

    public final boolean isTile() {
        return this.type == Type.TILE;
    }

    public EntityCollision getAsEntityCollision() {
        if (this.type != Type.ENTITY) {
            throw new IllegalStateException(String.format("Cannot convert collision of type \"%s\" to ENTITY",
                                                          this.type));
        }
        return (EntityCollision) this;
    }

    public TileCollision getAsTileCollision() {
        if (this.type != Type.TILE) {
            throw new IllegalStateException(String.format("Cannot convert collision of type \"%s\" to TILE",
                                                          this.type));
        }
        return (TileCollision) this;
    }

    private Collision(
            final Type type,
            final Mode mode
    ) {
        this.type = type;
        this.mode = mode;
    }

    public static Collision tile(
            final Mode mode,
            final double tileX,
            final double tileY
    ) {
        return new TileCollision(mode, tileX, tileY);
    }

    public static Collision entity(final Mode mode, final EntityHandle other) {
        return new EntityCollision(mode, other);
    }

    public enum Type {
        ENTITY,
        TILE
    }

    public enum Mode {
        OVERLAP,
        COLLISION
    }

    public static class EntityCollision extends Collision {
        private final EntityHandle other;

        public EntityHandle getOther() {
            return this.other;
        }

        private EntityCollision(final Mode mode, final EntityHandle other) {
            super(Type.ENTITY, mode);
            this.other = other;
        }
    }

    public static class TileCollision extends Collision {
        private final double tileX;
        private final double tileY;

        public double getTileX() {
            return this.tileX;
        }

        public double getTileY() {
            return this.tileY;
        }

        private TileCollision(final Mode mode, final double tileX, final double tileY) {
            super(Type.TILE, mode);
            this.tileX = tileX;
            this.tileY = tileY;
        }
    }
}
