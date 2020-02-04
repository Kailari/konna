package fi.jakojaannos.roguelite.game.systems.cleanup;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

public class CleanUpDeadEnemyKillsSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CLEANUP)
                    .tickBefore(ReaperSystem.class)
                    .requireResource(SessionStats.class)
                    .withComponent(EnemyTag.class)
                    .withComponent(AttackAbility.class)
                    .withComponent(DeadTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var sessionStats = world.getOrCreateResource(SessionStats.class);
        entities.map(entity -> world.getEntityManager().getComponentOf(entity, AttackAbility.class).orElseThrow())
                .map(abilities -> abilities.damageSource)
                .forEach(sessionStats::clearKillsOf);
    }
}
