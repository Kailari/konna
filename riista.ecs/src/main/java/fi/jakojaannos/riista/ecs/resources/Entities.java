package fi.jakojaannos.riista.ecs.resources;

import fi.jakojaannos.riista.ecs.EntityHandle;

public interface Entities {
    EntityHandle createEntity(Object... components);
}
