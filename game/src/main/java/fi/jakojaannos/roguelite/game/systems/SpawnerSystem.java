package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.annotation.DisableOn;
import fi.jakojaannos.roguelite.engine.ecs.annotation.DisabledByDefault;
import fi.jakojaannos.roguelite.engine.ecs.annotation.EnableOn;
import fi.jakojaannos.roguelite.engine.ecs.data.resources.Entities;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.SpawnerComponent;
import fi.jakojaannos.roguelite.game.data.events.HordeEndEvent;
import fi.jakojaannos.roguelite.game.data.events.HordeStartEvent;

@DisabledByDefault
public class SpawnerSystem implements EcsSystem<SpawnerSystem.Resources, SpawnerSystem.EntityData, SpawnerSystem.EventData> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final EventData eventData
    ) {
        final var delta = resources.timeManager.getTimeStepInSeconds();

        entities.forEach(entity -> {
            final var transform = entity.getData().transform;
            final var spawner = entity.getData().spawner;

            spawner.spawnCoolDown -= delta;

            if (spawner.spawnCoolDown <= 0.0f) {
                spawner.spawnCoolDown = spawner.spawnFrequency;
                spawner.entityFactory.get(resources.entities, transform, spawner);
            }
        });
    }

    public static record EntityData(
            SpawnerComponent spawner,
            Transform transform
    ) {}

    public static record Resources(
            TimeManager timeManager,
            Entities entities
    ) {}

    public static record EventData(
            @EnableOn HordeStartEvent hordeStart,
            @DisableOn HordeEndEvent hordeEnd
    ) {}
}
