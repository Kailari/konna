package fi.jakojaannos.roguelite.engine.view.ui;

import org.joml.Vector2i;

import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.view.data.components.ui.Color;

public final record UIProperty<T>(
        String name,
        @Nullable T defaultValue
) {
    // Generic
    public static final UIProperty<UIElementType<?>> TYPE = new UIProperty<>("type", UIElementType.NONE);
    public static final UIProperty<String> NAME = new UIProperty<>("name", "undefined");
    public static final UIProperty<Boolean> HIDDEN = new UIProperty<>("hidden", false);

    public static final UIProperty<ProportionValue> ANCHOR_X = new UIProperty<>("anchor_x", ProportionValue.notSet());
    public static final UIProperty<ProportionValue> ANCHOR_Y = new UIProperty<>("anchor_y", ProportionValue.notSet());
    public static final UIProperty<ProportionValue> LEFT = new UIProperty<>("left", ProportionValue.notSet());
    public static final UIProperty<ProportionValue> RIGHT = new UIProperty<>("right", ProportionValue.notSet());
    public static final UIProperty<ProportionValue> TOP = new UIProperty<>("top", ProportionValue.notSet());
    public static final UIProperty<ProportionValue> BOTTOM = new UIProperty<>("bottom", ProportionValue.notSet());
    public static final UIProperty<ProportionValue> WIDTH = new UIProperty<>("width", ProportionValue.notSet());
    public static final UIProperty<ProportionValue> HEIGHT = new UIProperty<>("height", ProportionValue.notSet());

    public static final UIProperty<Vector2i> CENTER = new UIProperty<>("center", null);

    // Panel
    public static final UIProperty<String> SPRITE = new UIProperty<>("sprite", null);
    public static final UIProperty<Integer> BORDER_SIZE = new UIProperty<>("borderSize", 5);

    // Label
    public static final UIProperty<String> TEXT = new UIProperty<>("text", null);
    public static final UIProperty<Color> COLOR = new UIProperty<>("color", new Color(1.0, 1.0, 1.0));
    public static final UIProperty<Integer> FONT_SIZE = new UIProperty<>("fontSize", 12);

    // Progress Bar
    public static final UIProperty<Double> PROGRESS = new UIProperty<>("progress", null);
    public static final UIProperty<Double> MAX_PROGRESS = new UIProperty<>("maxProgress", null);
}
