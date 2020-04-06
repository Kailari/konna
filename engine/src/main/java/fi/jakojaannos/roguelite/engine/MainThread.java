package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.roguelite.engine.ecs.legacy.ProvidedResource;

public interface MainThread extends ProvidedResource {
    void queueTask(MainThreadTask task);
}
