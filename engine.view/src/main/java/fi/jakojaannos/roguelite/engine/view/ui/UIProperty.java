package fi.jakojaannos.roguelite.engine.view.ui;

import fi.jakojaannos.roguelite.engine.view.data.components.internal.FontSize;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.Name;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.label.Text;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.panel.BorderSize;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.panel.PanelSprite;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.view.ui.internal.ComponentBackedUIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.internal.InstanceMappedUIProperty;

import java.util.Optional;

public interface UIProperty<T> {
    // Generic
    UIProperty<String> NAME = new ComponentBackedUIProperty<>("name", Name.class, Name::getValue, Name::setValue);
    UIProperty<Boolean> HIDDEN = new InstanceMappedUIProperty<>("hidden", false);
    UIProperty<Integer> MIN_X = new ComponentBackedUIProperty<>("minX", ElementBoundaries.class, ElementBoundaries::getMinX, ElementBoundaries::setMinX);
    UIProperty<Integer> MAX_X = new ComponentBackedUIProperty<>("maxX", ElementBoundaries.class, ElementBoundaries::getMaxX, ElementBoundaries::setMaxX);
    UIProperty<Integer> MIN_Y = new ComponentBackedUIProperty<>("minY", ElementBoundaries.class, ElementBoundaries::getMinY, ElementBoundaries::setMinY);
    UIProperty<Integer> MAX_Y = new ComponentBackedUIProperty<>("maxY", ElementBoundaries.class, ElementBoundaries::getMaxY, ElementBoundaries::setMaxY);
    UIProperty<Integer> WIDTH = new ComponentBackedUIProperty<>("width", ElementBoundaries.class, ElementBoundaries::getWidth, ElementBoundaries::setWidth);
    UIProperty<Integer> HEIGHT = new ComponentBackedUIProperty<>("height", ElementBoundaries.class, ElementBoundaries::getHeight, ElementBoundaries::setHeight);

    // Panel
    UIProperty<String> SPRITE = new ComponentBackedUIProperty<>("sprite", PanelSprite.class, PanelSprite::getSprite, PanelSprite::setSprite);
    UIProperty<Integer> BORDER_SIZE = new ComponentBackedUIProperty<>("borderSize", BorderSize.class, BorderSize::getValue, BorderSize::setValue);

    // Label
    UIProperty<String> TEXT = new ComponentBackedUIProperty<>("text", Text.class, Text::getText, Text::setText);
    UIProperty<Integer> FONT_SIZE = new ComponentBackedUIProperty<>("fontSize", FontSize.class, FontSize::getValue, FontSize::setValue);

    String getName();

    Optional<T> getFor(UIElement uiElement);

    void set(UIElement uiElement, T value);
}
