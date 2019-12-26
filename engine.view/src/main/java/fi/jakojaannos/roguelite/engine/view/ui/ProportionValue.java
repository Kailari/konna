package fi.jakojaannos.roguelite.engine.view.ui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Utility for converting human-readable values from UI definition to values understood by the
 * rendering system.
 */
public abstract class ProportionValue {
    public abstract int getValue(final Context context);

    public static ProportionValue percent(final double value) {
        return new Percent(value);
    }

    public static ProportionValue absolute(final int value) {
        return new Absolute(value);
    }

    // TODO: "em"-style proportional-to-font-size proportion value

    @RequiredArgsConstructor
    private static class Percent extends ProportionValue {
        private final double value;

        @Override
        public int getValue(final Context context) {
            return (int) (this.value * context.getParentSize());
        }
    }

    @RequiredArgsConstructor
    private static class Absolute extends ProportionValue {
        private final int value;

        @Override
        public int getValue(final Context context) {
            return this.value;
        }
    }

    @RequiredArgsConstructor
    public static final class Context {
        @Getter private final int fontSize;
        @Getter private final int parentSize;
    }
}
