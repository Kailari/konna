package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.FollowerEnemyAI;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;

import java.util.Random;
import java.util.stream.Stream;

@Slf4j
public class FollowerAIControllerSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.INPUT)
                    .requireResource(Players.class)
                    .withComponent(FollowerEnemyAI.class)
                    .withComponent(CharacterInput.class)
                    .withComponent(Transform.class);
    }

    private final Vector2d tmpDirection = new Vector2d();

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val player = world.getOrCreateResource(Players.class).getPlayer();
        if (player == null) {
            entities.forEach(entity -> world.getEntityManager()
                                            .getComponentOf(entity, CharacterInput.class)
                                            .orElseThrow().move.set(new Random().nextDouble() * 2 - 1.0,
                                                                    new Random().nextDouble() * 2 - 1.0));
            return;
        }

        val playerPos = world.getEntityManager()
                             .getComponentOf(player, Transform.class)
                             .orElseThrow().position;

        entities.forEach(entity -> {
            val aiPos = world.getEntityManager()
                             .getComponentOf(entity, Transform.class)
                             .orElseThrow().position;

            tmpDirection.set(playerPos).sub(aiPos);
            val input = world.getEntityManager()
                             .getComponentOf(entity, CharacterInput.class)
                             .orElseThrow();
            input.move.set(tmpDirection);
        });
    }
}
