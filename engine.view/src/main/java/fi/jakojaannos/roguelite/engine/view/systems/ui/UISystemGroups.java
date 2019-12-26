package fi.jakojaannos.roguelite.engine.view.systems.ui;

import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;

public enum UISystemGroups implements SystemGroup {
    PREPARATIONS,
    RENDERING;

    @Override
    public String getName() {
        return name();
    }
}
