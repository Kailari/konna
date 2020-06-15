package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.annotation.DisabledByDefault;
import fi.jakojaannos.roguelite.engine.ecs.annotation.EnableOn;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.riista.data.events.StateEvent;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.events.GameLostEvent;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.gamemode.GameplayGameMode;
import fi.jakojaannos.roguelite.game.gamemode.MainMenuGameMode;

@DisabledByDefault
public class RestartGameSystem implements EcsSystem<RestartGameSystem.Resources, EcsSystem.NoEntities, RestartGameSystem.EventData> {
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
                     .fire(new StateEvent.ChangeMode(GameplayGameMode.create(System.nanoTime(),
                                                                             resources.timeManager)));
        } else if (inputs.inputMenu) {
            resources.events()
                     .state()
                     .fire(new StateEvent.ChangeMode(MainMenuGameMode.create()));
        }
    }

    public static record Resources(Inputs inputs, Events events, TimeManager timeManager) {}

    public static record EventData(@EnableOn GameLostEvent gameLost) {}
}
