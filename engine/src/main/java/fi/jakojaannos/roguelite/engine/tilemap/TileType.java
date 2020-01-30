package fi.jakojaannos.roguelite.engine.tilemap;

import lombok.Getter;

public class TileType {
    @Getter private final int typeIndex;
    @Getter private final boolean solid;

    public TileType(final int typeIndex, final boolean solid) {
        this.typeIndex = typeIndex;
        this.solid = solid;
    }
}
