package fi.jakojaannos.roguelite.engine.view.ui;

import org.joml.Vector2i;

import java.util.Optional;

import fi.jakojaannos.roguelite.engine.view.data.components.internal.*;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.label.Text;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.panel.BorderSize;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.panel.PanelSprite;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.view.ui.internal.ComponentBackedUIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.internal.ElementBoundaryUIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.internal.InstanceMappedUIProperty;

// HACK: For suppressing checkstyle warnings about line length
@SuppressWarnings({"UnnecessaryInterfaceModifier", "checkstyle:RedundantModifier", "CheckStyle"})
public interface UIProperty<T> {
    // Generic
    public static final UIProperty<UIElementType<?>> TYPE = new InstanceMappedUIProperty<>("type", UIElementType.NONE);
    public static final UIProperty<String> NAME = new ComponentBackedUIProperty<>("name", Name.class, Name::getValue, Name::setValue);
    public static final UIProperty<Boolean> HIDDEN = new InstanceMappedUIProperty<>("hidden", false);

    // FIXME: Calling set on maxX/Y sets the maximum bound to an incorrect value
    public static final UIProperty<Integer> MIN_X = new ElementBoundaryUIProperty<>("minX", BoundLeft.class, BoundLeft::new, ElementBoundaries::getMinX, ElementBoundaries::setMinX);
    public static final UIProperty<Integer> MAX_X = new ElementBoundaryUIProperty<>("maxX", BoundRight.class, BoundRight::new, ElementBoundaries::getMaxX, ElementBoundaries::setMaxX);
    public static final UIProperty<Integer> MIN_Y = new ElementBoundaryUIProperty<>("minY", BoundTop.class, BoundTop::new, ElementBoundaries::getMinY, ElementBoundaries::setMinY);
    public static final UIProperty<Integer> MAX_Y = new ElementBoundaryUIProperty<>("maxY", BoundBottom.class, BoundBottom::new, ElementBoundaries::getMaxY, ElementBoundaries::setMaxY);
    public static final UIProperty<Integer> WIDTH = new ElementBoundaryUIProperty<>("width", BoundWidth.class, BoundWidth::new, ElementBoundaries::getWidth, ElementBoundaries::setWidth);
    public static final UIProperty<Integer> HEIGHT = new ElementBoundaryUIProperty<>("height", BoundHeight.class, BoundHeight::new, ElementBoundaries::getHeight, ElementBoundaries::setHeight);
    public static final UIProperty<Vector2i> CENTER = new ComponentBackedUIProperty<>("center", ElementBoundaries.class, ElementBoundaries::getCenter, (a, b) -> {
        throw new UnsupportedOperationException("Center is a calculated property. It cannot be set.");
    });

    // Panel
    public static final UIProperty<String> SPRITE = new ComponentBackedUIProperty<>("sprite", PanelSprite.class, PanelSprite::getSprite, PanelSprite::setSprite);
    public static final UIProperty<Integer> BORDER_SIZE = new ComponentBackedUIProperty<>("borderSize", BorderSize.class, BorderSize::getValue, BorderSize::setValue);

    // Label
    public static final UIProperty<String> TEXT = new ComponentBackedUIProperty<>("text", Text.class, Text::getText, Text::setText);
    public static final UIProperty<Integer> FONT_SIZE = new ComponentBackedUIProperty<>("fontSize", FontSize.class, FontSize::getValue, FontSize::setValue);

    // Progress Bar
    public static final UIProperty<Double> PROGRESS = new InstanceMappedUIProperty<>("progress", null);
    public static final UIProperty<Double> MAX_PROGRESS = new InstanceMappedUIProperty<>("maxProgress", null);

    String getName();

    Optional<T> getFor(UIElement uiElement);

    void set(UIElement uiElement, T value);
}
