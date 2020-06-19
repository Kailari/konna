package fi.jakojaannos.riista;

import fi.jakojaannos.riista.ecs.SystemState;
import fi.jakojaannos.riista.ecs.World;

public record GameState(
        World world,
        SystemState systems
) {
}
