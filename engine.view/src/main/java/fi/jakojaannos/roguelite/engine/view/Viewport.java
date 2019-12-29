package fi.jakojaannos.roguelite.engine.view;

import fi.jakojaannos.roguelite.engine.ui.UserInterface;

public interface Viewport extends UserInterface.ViewportSizeProvider {
    void resize(int width, int height);
}
