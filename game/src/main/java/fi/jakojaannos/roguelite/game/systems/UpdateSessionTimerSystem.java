package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.legacy.World;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;

public class UpdateSessionTimerSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CLEANUP)
                    .requireResource(SessionStats.class)
                    .withComponent(PlayerTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var noPlayersAlive = entities.count() == 0;
        if (noPlayersAlive) {
            return;
        }

        final var timeManager = world.getResource(Time.class);
        final var sessionStats = world.getOrCreateResource(SessionStats.class);
        sessionStats.endTimeStamp = timeManager.getCurrentGameTime();
    }
}
