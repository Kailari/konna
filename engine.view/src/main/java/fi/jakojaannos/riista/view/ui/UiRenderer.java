package fi.jakojaannos.riista.view.ui;

import java.util.Collection;

import fi.jakojaannos.roguelite.engine.ui.UIEvent;

public interface UiRenderer {
    /**
     * Sets the value of an UI value property. These are used to populate UI element text field format variables while
     * drawing.
     *
     * @param key   name of the property
     * @param value the new value
     */
    void setValue(String key, Object value);

    Collection<UIEvent> draw(UiElement element);
}
