package fi.jakojaannos.konna.engine.view;

import fi.jakojaannos.konna.engine.view.ui.UiElement;

public interface UiRenderer {
    /**
     * Sets the value of an UI value property. These are used to populate UI element text field format variables while
     * drawing.
     *
     * @param key   name of the property
     * @param value the new value
     */
    void setValue(String key, Object value);

    void draw(UiElement element);
}
