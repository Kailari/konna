package fi.jakojaannos.roguelite.game.systems.collision;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.data.components.TileMapLayer;
import fi.jakojaannos.roguelite.game.data.resources.collision.Colliders;

public class TileColliderCollectorSystem implements EcsSystem<TileColliderCollectorSystem.Resources, TileColliderCollectorSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        resources.colliders.tileMapLayersWithCollision =
                entities.map(EntityDataHandle::getData)
                        .map(EntityData::tileMapLayer)
                        .filter(TileMapLayer::isCollisionEnabled)
                        .map(TileMapLayer::getTileMap)
                        .collect(Collectors.toUnmodifiableList());
    }

    public static record Resources(Colliders colliders) {}

    public static record EntityData(TileMapLayer tileMapLayer) {}
}
