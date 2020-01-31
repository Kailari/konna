package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.roguelite.engine.ecs.ProvidedResource;

public interface MainThread extends ProvidedResource {
    void queueTask(MainThreadTask task);
}
