package fi.jakojaannos.roguelite.engine.state;

public interface WritableTimeProvider extends TimeProvider {
    void updateTime();
}
