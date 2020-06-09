package fi.jakojaannos.roguelite.engine.ecs.data.resources;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;

public interface Entities {
    EntityHandle createEntity(Object... components);
}
