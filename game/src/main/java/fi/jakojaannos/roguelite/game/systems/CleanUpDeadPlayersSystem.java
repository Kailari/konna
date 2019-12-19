package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.PlayerTag;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import lombok.val;

import java.util.stream.Stream;

public class CleanUpDeadPlayersSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CLEANUP)
                    .withComponent(DeadTag.class)
                    .withComponent(PlayerTag.class)
                    .tickBefore(ReaperSystem.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val players = world.getOrCreateResource(Players.class);
        if (players.player == null) {
            return;
        }

        for (val entity : (Iterable<Entity>) entities::iterator) {
            // FIXME: Remove null-check and add support for multiple players
            if (players.player != null && entity.getId() == players.player.getId()) {
                players.player = null;
            }
        }
    }
}
