package fi.jakojaannos.roguelite.engine.view.ui;

import java.util.function.Function;

import fi.jakojaannos.roguelite.engine.view.data.components.ui.ElementBoundaries;

/**
 * Utility for converting human-readable values from UI definition to values understood by the rendering system.
 */
public abstract class ProportionValue {
    public static PercentBuilder percentOf() {
        return new PercentBuilder();
    }

    public static ProportionValue absolute(final int value) {
        return new Absolute(value);
    }

    public static ProportionValue notSet() {
        return null;
    }

    public abstract int getValue(Context context);

    // TODO: "em"-style proportional-to-font-size proportion value

    private static final class PercentOf extends ProportionValue {
        private final double value;
        private final Function<Context, Integer> sizeFunction;

        private PercentOf(
                final double value,
                final Function<Context, Integer> sizeFunction
        ) {
            this.value = value;
            this.sizeFunction = sizeFunction;
        }

        @Override
        public int getValue(final Context context) {
            return (int) (this.value * this.sizeFunction.apply(context));
        }
    }

    public static final class PercentOfSelf extends ProportionValue {
        private final double value;
        private final boolean horizontal;
        private final Function<Context, Integer> sizeFunction;

        public boolean isHorizontal() {
            return this.horizontal;
        }

        private PercentOfSelf(
                final double value,
                final boolean horizontal,
                final Function<Context, Integer> sizeFunction
        ) {
            this.value = value;
            this.horizontal = horizontal;
            this.sizeFunction = sizeFunction;
        }

        @Override
        public int getValue(final Context context) {
            final var sizeValue = this.sizeFunction.apply(context);
            if (sizeValue == ElementBoundaries.INVALID_VALUE) {
                throw new IllegalStateException("Tried to get proportional size of self in context where own size is " +
                                                        "not set!");
            }
            return (int) (this.value * this.sizeFunction.apply(context));
        }
    }

    private static class Absolute extends ProportionValue {
        private final int value;

        private Absolute(final int value) {
            this.value = value;
        }

        @Override
        public int getValue(final Context context) {
            return this.value;
        }
    }

    public static final record Context(
            int fontSize,
            ElementBoundaries parentBounds,
            ElementBoundaries ownBounds
    ) {
    }

    public static final class PercentBuilder {
        public ProportionValue parentWidth(final double value) {
            return new PercentOf(value, context -> context.parentBounds.width);
        }

        public ProportionValue parentHeight(final double value) {
            return new PercentOf(value, context -> context.parentBounds.height);
        }

        public ProportionValue ownWidth(final double value) {
            return new PercentOfSelf(value, true, context -> context.ownBounds.width);
        }

        public ProportionValue ownHeight(final double value) {
            return new PercentOfSelf(value, false, context -> context.ownBounds.height);
        }
    }
}
