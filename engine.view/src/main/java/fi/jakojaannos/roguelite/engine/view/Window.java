package fi.jakojaannos.roguelite.engine.view;

public interface Window {
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
