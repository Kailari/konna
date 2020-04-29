package fi.jakojaannos.roguelite.engine.view.ui.internal;

import java.util.function.Function;

import fi.jakojaannos.roguelite.engine.view.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;

public class ProportionValueImpl {
    public static ProportionValue.PercentGetter widthGetter(final double value, final boolean parent) {
        return new ProportionValue.PercentGetter(value,
                                                 !parent,
                                                 true,
                                                 context -> selectBounds(context, parent).getWidth());
    }

    public static ProportionValue.PercentGetter heightGetter(final double value, final boolean parent) {
        return new ProportionValue.PercentGetter(value,
                                                 !parent,
                                                 false,
                                                 context -> selectBounds(context, parent).getHeight());
    }

    public static ProportionValue percentOf(final ProportionValue.PercentGetter target) {
        if (target.relativeToSelf()) {
            return new PercentOfSelf(target.value(), target.horizontal(), target.getter());
        }

        return context -> (int) (target.value() * target.getter().apply(context));
    }

    public static ProportionValue absolute(final int value) {
        return context -> value;
    }

    private static ElementBoundaries selectBounds(
            final ProportionValue.Context context,
            final boolean parent
    ) {
        return parent ? context.parentBounds() : context.ownBounds();
    }

    public static final class PercentOfSelf implements ProportionValue {
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
}
