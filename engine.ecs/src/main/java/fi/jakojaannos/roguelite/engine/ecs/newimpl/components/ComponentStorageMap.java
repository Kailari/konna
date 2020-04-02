package fi.jakojaannos.roguelite.engine.ecs.newimpl.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentStorageMap {
    private final Map<Class<?>, List<?>> storages = new HashMap<>();
}
