package fi.jakojaannos.roguelite.engine.tilemap;

import org.joml.Vector2i;

import fi.jakojaannos.roguelite.engine.tilemap.storage.ChunkMap;

public class TileMap<TTile> {
    private final ChunkMap<TTile> chunks;

    public TileMap(final TTile defaultTile) {
        this.chunks = new ChunkMap<>(defaultTile);
    }

    public void setTile(final Vector2i coordinates, final TTile tileType) {
        setTile(coordinates.x, coordinates.y, tileType);
    }

    public void setTile(final int x, final int y, final TTile tileType) {
        final var chunk = this.chunks.getByTileCoordinates(x, y);
        chunk.setByTileCoordinates(x, y, tileType);
    }

    public TTile getTile(final Vector2i coordinates) {
        return getTile(coordinates.x, coordinates.y);
    }

    public TTile getTile(final int x, final int y) {
        return this.chunks.getByTileCoordinates(x, y)
                          .getByTileCoordinates(x, y);
    }
}
