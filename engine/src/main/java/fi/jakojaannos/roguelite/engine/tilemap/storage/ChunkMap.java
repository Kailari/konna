package fi.jakojaannos.roguelite.engine.tilemap.storage;

import java.util.HashMap;
import java.util.Map;

public class ChunkMap<TTile> {
    private final TTile defaultTile;
    private Map<Long, Chunk<TTile>> chunks = new HashMap<>();

    public ChunkMap(final TTile defaultTile) {
        this.defaultTile = defaultTile;
    }

    private static long packCoordinate(final int x, final int y) {
        return (((long) x) << 32) | (y & 0xffffffffL);
    }

    public Chunk<TTile> getByTileCoordinates(final int x, final int y) {
        final var chunkX = Math.floorDiv(x, Chunk.CHUNK_SIZE);
        final var chunkY = Math.floorDiv(y, Chunk.CHUNK_SIZE);
        return getByChunkCoordinates(chunkX, chunkY);
    }

    public Chunk<TTile> getByChunkCoordinates(final int chunkX, final int chunkY) {
        return this.chunks.computeIfAbsent(packCoordinate(chunkX, chunkY),
                                           key -> new Chunk<>(chunkX, chunkY, defaultTile));
    }
}
