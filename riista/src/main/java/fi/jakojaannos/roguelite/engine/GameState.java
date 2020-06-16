package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.riista.ecs.SystemState;
import fi.jakojaannos.riista.ecs.World;

public record GameState(
        World world,
        SystemState systems
) {
}
