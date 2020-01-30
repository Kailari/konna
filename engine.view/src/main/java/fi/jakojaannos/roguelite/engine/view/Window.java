package fi.jakojaannos.roguelite.engine.view;

public interface Window extends AutoCloseable {
    void show();

    int getWidth();

    int getHeight();

    void addResizeCallback(ResizeCallback callback);

    enum Mode {
        Windowed,
        FullScreen,
        Borderless,
    }

    interface ResizeCallback {
        void call(int width, int height);
    }
}
