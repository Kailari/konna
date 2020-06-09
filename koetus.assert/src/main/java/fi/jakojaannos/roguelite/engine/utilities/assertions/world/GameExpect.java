package fi.jakojaannos.roguelite.engine.utilities.assertions.world;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.builder.SimulationBuilderImpl;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.entity.EntityExpectImpl;

/**
 * Utilities for creating test game simulators and performing assertions on Game/ECS worlds.
 */
public final class GameExpect {
    private GameExpect() {
    }

    /**
     * Start a new test simulation builder. Name comes from the builder format which follows fluent syntax for <i>"When
     * game with X and Y runs for Z, expect W"</i>
     *
     * @return the builder
     */
    public static SimulationBuilder whenGame() {
        return new SimulationBuilderImpl();
    }

    /**
     * Start a new assertion builder for the given entity. Allows performing assertions on the entity and the state of
     * its components.
     *
     * @param entity the entity to perform assertions on
     *
     * @return the assertion builder
     *
     * @see EntityExpect
     */
    public static EntityExpect expectEntity(final EntityHandle entity) {
        return new EntityExpectImpl(entity);
    }
}
