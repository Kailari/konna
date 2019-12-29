package fi.jakojaannos.roguelite.engine.systems.ui;

import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;

public enum UISystemGroups implements SystemGroup {
    PREPARATIONS,
    EVENTS;

    @Override
    public String getName() {
        return name();
    }
}
