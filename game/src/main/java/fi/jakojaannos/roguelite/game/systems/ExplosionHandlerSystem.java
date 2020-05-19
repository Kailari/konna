package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;

public class ExplosionHandlerSystem implements EcsSystem<ExplosionHandlerSystem.Resources, ExplosionHandlerSystem.EntityData, EcsSystem.NoEvents> {


    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {

    }

    public static record EntityData(

    ) {}

    public static record Resources(

    ) {}
}
