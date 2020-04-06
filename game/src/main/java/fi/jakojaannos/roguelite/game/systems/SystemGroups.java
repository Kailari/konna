package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.legacy.SystemGroup;

@Deprecated
public enum SystemGroups implements SystemGroup {
    @Deprecated INPUT,
    @Deprecated EARLY_TICK,
    @Deprecated CHARACTER_TICK,
    @Deprecated PHYSICS_TICK,
    @Deprecated COLLISION_HANDLER,
    @Deprecated LATE_TICK,
    @Deprecated CLEANUP;

    @Override
    @Deprecated
    public String getName() {
        return this.name();
    }
}
