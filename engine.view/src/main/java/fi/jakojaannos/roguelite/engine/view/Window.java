package fi.jakojaannos.roguelite.engine.view;

public interface Window extends AutoCloseable {
    void show();

    enum Mode {
        Windowed,
        FullScreen,
        Borderless,
    }

    int getWidth();

    int getHeight();

    void addResizeCallback(ResizeCallback callback);

    interface ResizeCallback {
        void call(int width, int height);
    }
}
