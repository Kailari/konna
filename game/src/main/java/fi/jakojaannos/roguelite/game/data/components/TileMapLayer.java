package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;

public class TileMapLayer implements Component {
    public TileMap<TileType> tileMap;
    public boolean collisionEnabled;

    public TileMap<TileType> getTileMap() {
        return this.tileMap;
    }

    public boolean isCollisionEnabled() {
        return this.collisionEnabled;
    }

    public TileMapLayer(final TileMap<TileType> tileMap, final boolean collisionEnabled) {
        this.tileMap = tileMap;
        this.collisionEnabled = collisionEnabled;
    }
}
