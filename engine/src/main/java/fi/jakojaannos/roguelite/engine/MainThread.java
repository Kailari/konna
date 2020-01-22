package fi.jakojaannos.roguelite.engine;

public interface MainThread {
    void queueTask(MainThreadTask task);
}
