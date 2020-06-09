package fi.jakojaannos.roguelite.engine.view.ui.query;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;

public interface UIMatcher {
    boolean evaluate(UIElement uiElement);
}
