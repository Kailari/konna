package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
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
        // TODO: @DisableOn GameLostEvent
        final var noPlayersAlive = entities.count() == 0;
        if (noPlayersAlive) {
            return;
        }

        final var timeManager = world.fetchResource(TimeManager.class);
        final var sessionStats = world.fetchResource(SessionStats.class);
        sessionStats.endTimeStamp = timeManager.getCurrentGameTime();
    }
}
