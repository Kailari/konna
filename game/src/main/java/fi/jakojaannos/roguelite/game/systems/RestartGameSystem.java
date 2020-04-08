package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.state.StateEvent;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;
import fi.jakojaannos.roguelite.game.gamemode.GameplayGameMode;
import fi.jakojaannos.roguelite.game.gamemode.MainMenuGameMode;

// TODO: Use "Player dead event" to enable this system
public class RestartGameSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CLEANUP)
                    .requireResource(Inputs.class)
                    .requireResource(SessionStats.class)
                    .requireProvidedResource(TimeManager.class)
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

        final var inputs = world.fetchResource(Inputs.class);
        if (inputs.inputRestart) {
            world.fetchResource(Events.class)
                 .state()
                 .fire(new StateEvent.ChangeMode(GameplayGameMode.create(System.nanoTime())));
        } else if (inputs.inputMenu) {
            world.fetchResource(Events.class)
                 .state()
                 .fire(new StateEvent.ChangeMode(MainMenuGameMode.create()));
        }
    }
}
