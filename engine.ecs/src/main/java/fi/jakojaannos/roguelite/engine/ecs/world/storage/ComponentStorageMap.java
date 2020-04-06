package fi.jakojaannos.roguelite.engine.ecs.world.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentStorageMap {
    private final Map<Class<?>, List<?>> storages = new HashMap<>();
}
