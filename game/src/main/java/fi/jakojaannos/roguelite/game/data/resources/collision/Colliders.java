package fi.jakojaannos.roguelite.game.data.resources.collision;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.Collider;

public class Colliders {
    public final Map<CollisionLayer, Queue<ColliderEntity>> solidForLayer = new ConcurrentHashMap<>();
    public final Map<CollisionLayer, Queue<ColliderEntity>> overlapsWithLayer = new ConcurrentHashMap<>();
    public List<TileMap<TileType>> tileMapLayersWithCollision;

    public static record ColliderEntity(
            EntityHandle entity,
            Transform transform,
            Collider collider
    ) {
    }
}
