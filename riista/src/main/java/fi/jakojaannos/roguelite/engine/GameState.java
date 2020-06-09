package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.roguelite.engine.ecs.SystemState;
import fi.jakojaannos.roguelite.engine.ecs.World;

public record GameState(
        World world,
        SystemState systems
) {
}
