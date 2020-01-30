package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;

public enum SystemGroups implements SystemGroup {
    INPUT,
    EARLY_TICK,
    CHARACTER_TICK,
    PHYSICS_TICK,
    COLLISION_HANDLER,
    LATE_TICK,
    CLEANUP;

    @Override
    public String getName() {
        return this.name();
    }
}
