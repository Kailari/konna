package fi.jakojaannos.roguelite.game.test.view;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import fi.jakojaannos.roguelite.engine.view.Window;

@AllArgsConstructor
public class TestWindow implements Window {
    private final List<ResizeCallback> resizeCallbacks = new ArrayList<>();
    @Getter private int width;
    @Getter private int height;

    public void resize(final int width, final int height) {
        this.width = width;
        this.height = height;
        this.resizeCallbacks.forEach(resizeCallback -> resizeCallback.call(width, height));
    }

    @Override
    public void show() {
    }

    @Override
    public void addResizeCallback(final ResizeCallback callback) {
        this.resizeCallbacks.add(callback);
    }

    @Override
    public void close() {
    }
}
