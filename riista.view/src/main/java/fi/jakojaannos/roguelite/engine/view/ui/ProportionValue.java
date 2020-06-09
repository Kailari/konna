package fi.jakojaannos.roguelite.engine.view.ui;

import java.util.function.Function;

import fi.jakojaannos.roguelite.engine.view.ui.internal.ProportionValueImpl;

/**
 * Utility for converting human-readable values from UI definition to values understood by the rendering system.
 */
public interface ProportionValue {
    static ProportionValue percentOf(final PercentGetter target) {
        return ProportionValueImpl.percentOf(target);
    }

    static ProportionValue absolute(final int value) {
        return ProportionValueImpl.absolute(value);
    }

    static ProportionValue notSet() {
        return null;
    }

    static PercentGetter parentWidth(final double value) {
        return ProportionValueImpl.widthGetter(value, true);
    }

    static PercentGetter parentHeight(final double value) {
        return ProportionValueImpl.heightGetter(value, true);
    }

    static PercentGetter ownWidth(final double value) {
        return ProportionValueImpl.widthGetter(value, false);
    }

    static PercentGetter ownHeight(final double value) {
        return ProportionValueImpl.heightGetter(value, false);
    }

    int getValue(Context context);

    // TODO: "em"-style proportional-to-font-size proportion value


    record PercentGetter(
            double value,
            boolean relativeToSelf,
            boolean horizontal,
            Function<Context, Integer>getter
    ) {}

    record Context(
            int fontSize,
            ElementBoundaries parentBounds,
            ElementBoundaries ownBounds
    ) {}
}
