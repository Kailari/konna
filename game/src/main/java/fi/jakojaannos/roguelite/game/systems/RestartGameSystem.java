package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.PlayerTag;
import fi.jakojaannos.roguelite.game.data.resources.GameStatus;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import lombok.val;

import java.util.stream.Stream;

public class RestartGameSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CLEANUP)
                    .requireResource(Inputs.class)
                    .requireResource(GameStatus.class)
                    .withComponent(PlayerTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val anyPlayerAlive = entities.count() > 0;
        if (anyPlayerAlive) {
            return;
        }

        val inputs = world.getOrCreateResource(Inputs.class);
        if (inputs.inputRestart) {
            world.getOrCreateResource(GameStatus.class).shouldRestart = true;
        }
    }
}
