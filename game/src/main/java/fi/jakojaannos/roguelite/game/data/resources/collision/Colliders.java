package fi.jakojaannos.roguelite.game.data.resources.collision;

import org.joml.Rectangled;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.TileMapLayer;
import fi.jakojaannos.roguelite.game.systems.physics.ApplyVelocitySystem;

public class Colliders {
    public final Map<CollisionLayer, List<ColliderEntity>> solidForLayer = new HashMap<>();
    public final Map<CollisionLayer, List<ColliderEntity>> overlapsWithLayer = new HashMap<>();
    public List<TileMap<TileType>> tileMapLayersWithCollision;

    public static record ColliderEntity(
            EntityHandle entity,
            Transform transform,
            Collider collider
    ) {
    }
}
