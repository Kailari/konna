package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.data.resources.Explosions;

public class ExplosionCleanupSystem implements EcsSystem<ExplosionCleanupSystem.Resources, EcsSystem.NoEntities, EcsSystem.NoEvents> {


    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<NoEntities>> entities,
            final NoEvents noEvents
    ) {

    }

    public static record Resources(
            Explosions explosions
    ) {}
}
