package fi.jakojaannos.roguelite.game.systems.cleanup;

import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.data.resources.Events;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.events.PlayerDeadEvent;
import fi.jakojaannos.roguelite.game.data.resources.Players;

public class CleanUpDeadPlayersSystem implements EcsSystem<CleanUpDeadPlayersSystem.Resources, CleanUpDeadPlayersSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        entities.forEach(entity -> {
            resources.events.fire(new PlayerDeadEvent());
            resources.players.removePlayer(entity.getHandle());
        });
    }

    public static record Resources(Players players, Events events) {}

    public static record EntityData(
            DeadTag deadTag,
            PlayerTag playerTag
    ) {}
}
