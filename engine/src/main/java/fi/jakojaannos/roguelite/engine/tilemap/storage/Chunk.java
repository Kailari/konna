package fi.jakojaannos.roguelite.engine.tilemap.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Chunk<TTile> {
    public static final int CHUNK_SIZE = 16;
    private static final int TILES_PER_CHUNK = CHUNK_SIZE * CHUNK_SIZE;

    private final int[] tileData = new int[TILES_PER_CHUNK];

    private transient final List<TTile> tileTypes = new ArrayList<>();
    private transient final Map<TTile, Integer> tileTypeToIndex = new HashMap<>();

    public Chunk(final TTile defaultTileType) {
        this.tileTypes.add(defaultTileType);
        this.tileTypeToIndex.put(defaultTileType, 0);
    }

    private int local(final int coord) {
        return Math.abs(coord % CHUNK_SIZE);
    }

    public TTile getByTileCoordinates(
            final int x,
            final int y
    ) {
        return getByLocal(local(x), local(y));
    }

    public void setByTileCoordinates(
            final int x,
            final int y,
            final TTile tileType
    ) {
        setByLocal(local(x), local(y), tileType);
    }

    private int index(final int x, final int y) {
        return y * CHUNK_SIZE + x;
    }

    private TTile getByLocal(final int localX, final int localY) {
        return this.tileTypes.get(this.tileData[index(localX, localY)]);
    }

    private void setByLocal(final int localX, final int localY, final TTile tileType) {
        this.tileData[index(localX, localY)] = resolveTileTypeIndex(tileType);
    }

    private int resolveTileTypeIndex(final TTile tileType) {
        return this.tileTypeToIndex.computeIfAbsent(tileType,
                type -> {
                    final var index = this.tileTypes.size();
                    this.tileTypes.add(type);
                    return index;
                });
    }
}
