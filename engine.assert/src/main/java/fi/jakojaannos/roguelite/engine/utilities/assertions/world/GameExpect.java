package fi.jakojaannos.roguelite.engine.utilities.assertions.world;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.builder.SimulationBuilderImpl;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.entity.EntityExpectImpl;

public final class GameExpect {
    public static SimulationBuilder whenGame() {
        return new SimulationBuilderImpl();
    }

    public static EntityExpect expectEntity(final EntityHandle entity) {
        return new EntityExpectImpl(entity);
    }
}
