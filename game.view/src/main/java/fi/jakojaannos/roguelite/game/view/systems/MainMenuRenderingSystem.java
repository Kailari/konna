package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;

import java.util.stream.Stream;

public class MainMenuRenderingSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {

    }
}
