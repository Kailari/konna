package fi.jakojaannos.roguelite.game.systems.collision;

import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import fi.jakojaannos.roguelite.game.data.components.weapon.ProjectileStats;
import fi.jakojaannos.roguelite.game.data.resources.collision.Collisions;

public class DestroyProjectilesOnCollisionSystem implements EcsSystem<DestroyProjectilesOnCollisionSystem.Resources, DestroyProjectilesOnCollisionSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var collisions = resources.collisions;

        entities.forEach(entity -> {
            if (collisions.getEventsFor(entity.getHandle())
                          .stream()
                          .map(CollisionEvent::collision)
                          .anyMatch(c -> c.getMode() == Collision.Mode.COLLISION)) {
                entity.destroy();
            }
        });
    }

    public static record Resources(Collisions collisions) {}

    public static record EntityData(ProjectileStats projectile, RecentCollisionTag recentCollisionTag) {}
}
