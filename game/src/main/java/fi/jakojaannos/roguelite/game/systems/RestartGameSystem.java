package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.GameStateManager;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;
import fi.jakojaannos.roguelite.game.state.MainMenuGameState;

public class RestartGameSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CLEANUP)
                    .requireResource(Inputs.class)
                    .requireResource(SessionStats.class)
                    .requireResource(GameStateManager.class)
                    .requireProvidedResource(Time.class)
                    .withComponent(PlayerTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var anyPlayerAlive = entities.count() > 0;
        if (anyPlayerAlive) {
            return;
        }

        final var inputs = world.getOrCreateResource(Inputs.class);
        if (inputs.inputRestart) {
            world.getOrCreateResource(GameStateManager.class)
                 .queueStateChange(new GameplayGameState(System.nanoTime(),
                                                         fi.jakojaannos.roguelite.engine.ecs.newecs.World.createNew(),
                                                         world.getResource(Time.class).timeManager()));
        } else if (inputs.inputMenu) {
            world.getOrCreateResource(GameStateManager.class)
                 .queueStateChange(new MainMenuGameState(fi.jakojaannos.roguelite.engine.ecs.newecs.World.createNew(),
                                                         world.getResource(Time.class).timeManager()));
        }
    }
}
