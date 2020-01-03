package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.resources.GameStateManager;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.PlayerTag;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;
import fi.jakojaannos.roguelite.game.state.MainMenuGameState;
import lombok.val;

import java.util.stream.Stream;

public class RestartGameSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CLEANUP)
                    .requireResource(Inputs.class)
                    .requireResource(SessionStats.class)
                    .requireResource(GameStateManager.class)
                    .requireResource(Time.class)
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
            world.getOrCreateResource(GameStateManager.class)
                 .queueStateChange(new GameplayGameState(System.nanoTime(),
                                                         World.createNew(EntityManager.createNew(256, 32)),
                                                         world.getOrCreateResource(Time.class).getTimeManager()));
        } else if (inputs.inputMenu) {
            world.getOrCreateResource(GameStateManager.class)
                 .queueStateChange(new MainMenuGameState(World.createNew(EntityManager.createNew(256, 32)),
                                                         world.getOrCreateResource(Time.class).getTimeManager()));
        }
    }
}
