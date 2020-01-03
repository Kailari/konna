package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;

public enum RenderSystemGroups implements SystemGroup {
    LEVEL,
    ENTITIES,
    OVERLAY,
    UI,
    DEBUG;

    @Override
    public String getName() {
        return this.name();
    }
}
