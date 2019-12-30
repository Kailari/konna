package fi.jakojaannos.roguelite.engine.ui;

import fi.jakojaannos.roguelite.engine.data.components.internal.ui.FontSize;
import fi.jakojaannos.roguelite.engine.data.components.internal.ui.Name;
import fi.jakojaannos.roguelite.engine.data.components.internal.ui.label.Text;
import fi.jakojaannos.roguelite.engine.data.components.internal.ui.panel.BorderSize;
import fi.jakojaannos.roguelite.engine.data.components.internal.ui.panel.PanelSprite;
import fi.jakojaannos.roguelite.engine.data.components.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.ui.internal.ComponentBackedUIProperty;

public interface UIProperty<T> {
    // Generic
    UIProperty<String> NAME = new ComponentBackedUIProperty<>("name", Name.class, name -> name.value);
    UIProperty<Integer> MIN_X = new ComponentBackedUIProperty<>("minX", ElementBoundaries.class, bounds -> bounds.minX);
    UIProperty<Integer> MAX_X = new ComponentBackedUIProperty<>("maxX", ElementBoundaries.class, bounds -> bounds.maxX);
    UIProperty<Integer> MIN_Y = new ComponentBackedUIProperty<>("minY", ElementBoundaries.class, bounds -> bounds.minY);
    UIProperty<Integer> MAX_Y = new ComponentBackedUIProperty<>("maxY", ElementBoundaries.class, bounds -> bounds.maxY);
    UIProperty<Integer> WIDTH = new ComponentBackedUIProperty<>("width", ElementBoundaries.class, bounds -> bounds.width);
    UIProperty<Integer> HEIGHT = new ComponentBackedUIProperty<>("height", ElementBoundaries.class, bounds -> bounds.height);

    // Panel
    UIProperty<String> SPRITE = new ComponentBackedUIProperty<>("sprite", PanelSprite.class, panelSprite -> panelSprite.sprite);
    UIProperty<Integer> BORDER_SIZE = new ComponentBackedUIProperty<>("borderSize", BorderSize.class, borderSize -> borderSize.value);

    // Label
    UIProperty<String> TEXT = new ComponentBackedUIProperty<>("text", Text.class, text -> text.text);
    UIProperty<Integer> FONT_SIZE = new ComponentBackedUIProperty<>("fontSize", FontSize.class, fontSize -> fontSize.value);

    String getName();
}
