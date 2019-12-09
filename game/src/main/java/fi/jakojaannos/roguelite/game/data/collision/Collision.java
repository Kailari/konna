package fi.jakojaannos.roguelite.game.data.collision;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import lombok.Getter;

public abstract class Collision {
    public enum Type {
        ENTITY,
        TILE
    }

    public enum Mode {
        OVERLAP,
        COLLISION
    }

    @Getter private final Type type;
    @Getter private final Mode mode;

    public final boolean isEntity() {
        return this.type == Type.ENTITY;
    }

    public final boolean isTile() {
        return this.type == Type.TILE;
    }

    public EntityCollision getAsEntityCollision() {
        if (this.type != Type.ENTITY) {
            throw new IllegalStateException(String.format("Cannot convert collision of type \"%s\" to ENTITY", this.type));
        }
        return (EntityCollision) this;
    }

    public TileCollision getAsTileCollision() {
        if (this.type != Type.TILE) {
            throw new IllegalStateException(String.format("Cannot convert collision of type \"%s\" to TILE", this.type));
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

    public static Collision entity(final Mode mode, final Entity other) {
        return new EntityCollision(mode, other);
    }

    public static class EntityCollision extends Collision {
        @Getter private final Entity other;

        private EntityCollision(final Mode mode, final Entity other) {
            super(Type.ENTITY, mode);
            this.other = other;
        }
    }

    public static class TileCollision extends Collision {
        @Getter private final double tileX, tileY;

        private TileCollision(final Mode mode, final double tileX, final double tileY) {
            super(Type.TILE, mode);
            this.tileX = tileX;
            this.tileY = tileY;
        }
    }
}
