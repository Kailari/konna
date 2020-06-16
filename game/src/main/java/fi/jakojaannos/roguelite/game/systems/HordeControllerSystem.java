package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.events.HordeEndEvent;
import fi.jakojaannos.roguelite.game.data.events.HordeStartEvent;
import fi.jakojaannos.roguelite.game.data.events.HordeStopEvent;
import fi.jakojaannos.roguelite.game.data.resources.Horde;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;

public class HordeControllerSystem implements EcsSystem<HordeControllerSystem.Resources, HordeControllerSystem.EntityData, EcsSystem.NoEvents> {
    private final long initialCalm;
    private final long calmDuration;
    private final long baseHordeDuration;
    private final double hordeDifficultyMultiplier;

    public HordeControllerSystem(
            final long initialCalm,
            final long calmDuration,
            final long baseHordeDuration,
            final double hordeDifficultyMultiplier
    ) {
        this.initialCalm = initialCalm;
        this.calmDuration = calmDuration;

        this.baseHordeDuration = baseHordeDuration;
        this.hordeDifficultyMultiplier = hordeDifficultyMultiplier;
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var currentTime = resources.timeManager.getCurrentGameTime();
        final var timeSinceSessionStart = currentTime - resources.sessionStats.beginTimeStamp;
        if (timeSinceSessionStart < this.initialCalm) {
            return;
        }

        final var horde = resources.horde;

        final var timeSinceLastHorde = currentTime - horde.endTimestamp;

        final var hordeActive = horde.startTimestamp >= horde.endTimestamp;
        if (timeSinceLastHorde >= this.calmDuration && !hordeActive) {
            startHorde(resources, currentTime);
        }

        final var timeSinceHordeStart = currentTime - horde.startTimestamp;

        final var difficulty = horde.hordeIndex * this.hordeDifficultyMultiplier;
        final var currentHordeDuration = difficulty * this.baseHordeDuration;

        if (timeSinceHordeStart >= currentHordeDuration) {
            final var spawnersActive = horde.startTimestamp == horde.changeTimestamp;
            if (spawnersActive) {
                stopHordeSpawns(resources, currentTime);
            }

            final var noEnemiesRemaining = entities.count() == 0;
            if (noEnemiesRemaining && hordeActive) {
                endHorde(resources, currentTime);
            }
        }
    }

    private static void startHorde(final Resources resources, final long currentTime) {
        resources.horde.status = Horde.Status.ACTIVE;
        resources.horde.startTimestamp = currentTime;
        resources.horde.changeTimestamp = currentTime;
        ++resources.horde.hordeIndex;

        resources.events.system().fire(new HordeStartEvent());
    }

    private static void stopHordeSpawns(final Resources resources, final long currentTime) {
        resources.horde.status = Horde.Status.ENDING;
        resources.horde.changeTimestamp = currentTime;

        resources.events.system().fire(new HordeStopEvent());
    }

    private static void endHorde(final Resources resources, final long currentTime) {
        resources.horde.status = Horde.Status.INACTIVE;
        resources.horde.endTimestamp = currentTime;
        resources.horde.changeTimestamp = currentTime;

        resources.events.system().fire(new HordeEndEvent());
    }

    public static record EntityData(
            EnemyTag enemyTag
    ) {}

    public static record Resources(
            TimeManager timeManager,
            SessionStats sessionStats,
            Horde horde,
            Events events
    ) {}
}
