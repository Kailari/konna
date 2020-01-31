package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;

public class EnemyAttackCoolDownSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CHARACTER_TICK)
                    .withComponent(Transform.class)
                    .withComponent(CharacterAbilities.class)
                    .withComponent(EnemyTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var delta = world.getResource(Time.class).getTimeStepInSeconds();

        final var entityManager = world.getEntityManager();
        entities.forEach(entity -> entityManager.getComponentOf(entity, CharacterAbilities.class)
                                                .orElseThrow().attackTimer += delta);
    }
}
