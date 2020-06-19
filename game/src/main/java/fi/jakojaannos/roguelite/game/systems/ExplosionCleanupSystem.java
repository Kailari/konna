package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.data.resources.Explosions;

public class ExplosionCleanupSystem implements EcsSystem<ExplosionCleanupSystem.Resources, EcsSystem.NoEntities, EcsSystem.NoEvents> {

    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<NoEntities>> entities,
            final NoEvents noEvents
    ) {
        resources.explosions.clear();
    }

    public static record Resources(
            Explosions explosions
    ) {}
}
