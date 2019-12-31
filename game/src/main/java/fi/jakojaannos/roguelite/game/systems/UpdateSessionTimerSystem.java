package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.PlayerTag;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;
import lombok.val;

import java.util.stream.Stream;

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
        val noPlayersAlive = entities.count() == 0;
        if (noPlayersAlive) {
            return;
        }

        val timeManager = world.getOrCreateResource(Time.class);
        val sessionStats = world.getOrCreateResource(SessionStats.class);
        sessionStats.endTimeStamp = timeManager.getCurrentGameTime();
    }
}
