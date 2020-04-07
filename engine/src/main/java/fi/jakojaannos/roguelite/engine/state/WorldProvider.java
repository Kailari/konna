package fi.jakojaannos.roguelite.engine.state;

import fi.jakojaannos.roguelite.engine.ecs.legacy.LegacyWorld;

public interface WorldProvider {
    LegacyWorld getWorld();
}
