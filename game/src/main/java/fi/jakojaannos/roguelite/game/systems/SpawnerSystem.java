package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.ecs.annotation.DisableOn;
import fi.jakojaannos.riista.ecs.annotation.DisabledByDefault;
import fi.jakojaannos.riista.ecs.annotation.EnableOn;
import fi.jakojaannos.riista.ecs.resources.Entities;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.SpawnerComponent;
import fi.jakojaannos.roguelite.game.data.events.HordeStartEvent;
import fi.jakojaannos.roguelite.game.data.events.HordeStopEvent;
import fi.jakojaannos.roguelite.game.data.resources.Horde;

@DisabledByDefault
public class SpawnerSystem implements EcsSystem<SpawnerSystem.Resources, SpawnerSystem.EntityData, SpawnerSystem.EventData> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final EventData eventData
    ) {
        final var currentTime = resources.timeManager.getCurrentGameTime();

        entities.forEach(entity -> {
            final var transform = entity.getData().transform;
            final var spawner = entity.getData().spawner;

            final var hordeRelativeTimestamp = Math.max(resources.horde.startTimestamp - spawner.timeBetweenSpawns,
                                                        spawner.spawnTimestamp);
            final var timeSinceLastSpawn = currentTime - hordeRelativeTimestamp;
            if (timeSinceLastSpawn >= spawner.timeBetweenSpawns) {
                spawner.spawnTimestamp = currentTime;
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
            Entities entities,
            Horde horde
    ) {
    }

    public static record EventData(
            @EnableOn HordeStartEvent hordeStart,
            @DisableOn HordeStopEvent hordeEnd
    ) {}
}
