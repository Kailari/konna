package fi.jakojaannos.roguelite.game.systems.cleanup;

import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;

public class CleanUpDeadEnemyKillsSystem implements EcsSystem<CleanUpDeadEnemyKillsSystem.Resources, CleanUpDeadEnemyKillsSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        entities.map(entity -> entity.getData().attackAbility.damageSource)
                .forEach(resources.sessionStats::clearKillsOf);
    }

    public static record Resources(SessionStats sessionStats) {}

    public static record EntityData(
            EnemyTag enemyTag,
            AttackAbility attackAbility,
            DeadTag deadTag
    ) {}
}
