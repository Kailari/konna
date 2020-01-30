package fi.jakojaannos.roguelite.game.data.components;

import lombok.Getter;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;

public class TileMapLayer implements Component {
    @Getter public TileMap<TileType> tileMap;
    @Getter public boolean collisionEnabled = true;

    public TileMapLayer(final TileMap<TileType> tileMap) {
        this.tileMap = tileMap;
    }
}
