package fi.jakojaannos.roguelite.game.systems.cleanup;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.Requirements;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.resources.Players;

// TODO: EntityDestroyedEvent
public class CleanUpDeadPlayersSystem implements EcsSystem<CleanUpDeadPlayersSystem.Resources, CleanUpDeadPlayersSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public Requirements<Resources, EntityData, NoEvents> declareRequirements(
            final Requirements<Resources, EntityData, NoEvents> require
    ) {
        return require.entityData(EntityData.class)
                      .resources(Resources.class);
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        entities.forEach(entity -> resources.players.removePlayer(entity.getHandle()));
    }

    public static record Resources(Players players) {}

    public static record EntityData(
            DeadTag deadTag,
            PlayerTag playerTag
    ) {}
}
