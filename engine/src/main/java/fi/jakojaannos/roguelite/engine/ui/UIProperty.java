package fi.jakojaannos.roguelite.engine.ui;

import fi.jakojaannos.roguelite.engine.data.components.internal.ui.FontSize;
import fi.jakojaannos.roguelite.engine.data.components.internal.ui.label.Text;
import fi.jakojaannos.roguelite.engine.data.components.internal.ui.panel.BorderSize;
import fi.jakojaannos.roguelite.engine.data.components.internal.ui.panel.PanelSprite;
import fi.jakojaannos.roguelite.engine.data.components.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.ui.internal.ComponentBackedUIProperty;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public interface UIProperty<T> {
    // Generic
    UIProperty<Integer> MIN_X = new ComponentBackedUIProperty<>(ElementBoundaries.class, bounds -> bounds.minX);
    UIProperty<Integer> MAX_X = new ComponentBackedUIProperty<>(ElementBoundaries.class, bounds -> bounds.maxX);
    UIProperty<Integer> MIN_Y = new ComponentBackedUIProperty<>(ElementBoundaries.class, bounds -> bounds.minY);
    UIProperty<Integer> MAX_Y = new ComponentBackedUIProperty<>(ElementBoundaries.class, bounds -> bounds.maxY);
    UIProperty<Integer> WIDTH = new ComponentBackedUIProperty<>(ElementBoundaries.class, bounds -> bounds.width);
    UIProperty<Integer> HEIGHT = new ComponentBackedUIProperty<>(ElementBoundaries.class, bounds -> bounds.height);

    // Panel
    UIProperty<String> SPRITE = new ComponentBackedUIProperty<>(PanelSprite.class, panelSprite -> panelSprite.sprite);
    UIProperty<Integer> BORDER_SIZE = new ComponentBackedUIProperty<>(BorderSize.class, borderSize -> borderSize.value);

    // Label
    UIProperty<String> TEXT = new ComponentBackedUIProperty<>(Text.class, text -> text.text);
    UIProperty<Integer> FONT_SIZE = new ComponentBackedUIProperty<>(FontSize.class, fontSize -> fontSize.value);

    // "Black Magic"
    @SuppressWarnings("rawtypes")
    Collection<UIProperty> ALL = Arrays.stream(UIProperty.class.getFields())
                                       .filter(field -> Modifier.isPublic(field.getModifiers())
                                               && Modifier.isStatic(field.getModifiers())
                                               && Modifier.isFinal(field.getModifiers()))
                                       .filter(field -> UIProperty.class.isAssignableFrom(field.getType()))
                                       .map(field -> {
                                           try {
                                               return field.get(null); // field.get(static)
                                           } catch (IllegalAccessException e) {
                                               throw new IllegalStateException("Could not get UI Property constant \"" + field.getName() + "\" value!");
                                           }
                                       })
                                       .map(UIProperty.class::cast)
                                       .collect(Collectors.toList());
}
