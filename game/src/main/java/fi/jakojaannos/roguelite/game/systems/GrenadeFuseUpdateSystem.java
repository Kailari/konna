package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.weapon.Fuse;
import fi.jakojaannos.roguelite.game.data.components.weapon.GrenadeStats;
import fi.jakojaannos.roguelite.game.data.resources.Explosions;

public class GrenadeFuseUpdateSystem implements EcsSystem<GrenadeFuseUpdateSystem.Resources, GrenadeFuseUpdateSystem.EntityData, EcsSystem.NoEvents> {

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {

    }

    public static record EntityData(
            Transform transform,
            GrenadeStats stats,
            Fuse fuse
    ) {}

    public static record Resources(
            TimeManager timeManager,
            Explosions explosions
    ) {}
}
