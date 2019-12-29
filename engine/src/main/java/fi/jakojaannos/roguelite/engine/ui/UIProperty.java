package fi.jakojaannos.roguelite.engine.ui;

public interface UIProperty<T> {
    String getKey();

    T get();
}
