package fi.jakojaannos.roguelite.game.systems.cleanup;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;
import lombok.val;

import java.util.stream.Stream;

public class CleanUpDeadEnemyKillsSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CLEANUP)
                    .tickBefore(ReaperSystem.class)
                    .requireResource(SessionStats.class)
                    .withComponent(EnemyTag.class)
                    .withComponent(CharacterAbilities.class)
                    .withComponent(DeadTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val sessionStats = world.getOrCreateResource(SessionStats.class);
        entities.map(entity -> world.getEntityManager().getComponentOf(entity, CharacterAbilities.class).orElseThrow())
                .map(abilities -> abilities.damageSource)
                .forEach(sessionStats::clearKillsOf);
    }
}
