package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.Requirements;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.annotation.DisabledByDefault;
import fi.jakojaannos.roguelite.engine.ecs.annotation.EnableOn;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.state.StateEvent;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.events.GameLostEvent;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;
import fi.jakojaannos.roguelite.game.gamemode.GameplayGameMode;
import fi.jakojaannos.roguelite.game.gamemode.MainMenuGameMode;

@DisabledByDefault
public class RestartGameSystem implements EcsSystem<RestartGameSystem.Resources, EcsSystem.NoEntities, RestartGameSystem.EventData> {
    @Override
    public Requirements<Resources, NoEntities, EventData> declareRequirements(
            final Requirements<Resources, NoEntities, EventData> require
    ) {
        return require.resources(Resources.class)
                      .events(EventData.class);
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<NoEntities>> entities,
            final EventData eventData
    ) {
        final var inputs = resources.inputs;
        if (inputs.inputRestart) {
            resources.events()
                     .state()
                     .fire(new StateEvent.ChangeMode(GameplayGameMode.create(System.nanoTime())));
        } else if (inputs.inputMenu) {
            resources.events()
                     .state()
                     .fire(new StateEvent.ChangeMode(MainMenuGameMode.create()));
        }
    }

    public static record Resources(Inputs inputs, Events events) {}

    public static record EventData(@EnableOn GameLostEvent gameLost) {}
}
