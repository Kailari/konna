package fi.jakojaannos.riista.view.ui;

import java.util.Collection;

import fi.jakojaannos.riista.data.events.UiEvent;

public interface UiRenderer {
    /**
     * Sets the value of an UI value property. These are used to populate UI element text field format variables while
     * drawing.
     *
     * @param key   name of the property
     * @param value the new value
     */
    void setValue(String key, Object value);

    Collection<UiEvent> draw(UiElement element);

    Collection<UiEvent> draw(UiElement element, float x, float y, float w, float h);
}
