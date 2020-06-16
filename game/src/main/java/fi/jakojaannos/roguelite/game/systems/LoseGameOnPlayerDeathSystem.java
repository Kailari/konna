package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.game.data.events.GameLostEvent;
import fi.jakojaannos.roguelite.game.data.events.PlayerDeadEvent;

public class LoseGameOnPlayerDeathSystem implements EcsSystem<LoseGameOnPlayerDeathSystem.Resources, EcsSystem.NoEntities, LoseGameOnPlayerDeathSystem.EventData> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<NoEntities>> entities,
            final EventData eventData
    ) {
        resources.events.system().fire(new GameLostEvent());
    }

    public static record Resources(Events events) {}

    public static record EventData(PlayerDeadEvent playerDead) {}
}
