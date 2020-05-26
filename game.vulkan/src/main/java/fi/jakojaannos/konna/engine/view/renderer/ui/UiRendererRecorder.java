package fi.jakojaannos.konna.engine.view.renderer.ui;

import org.lwjgl.vulkan.VkExtent2D;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import fi.jakojaannos.konna.engine.application.PresentableState;
import fi.jakojaannos.konna.engine.view.Presentable;
import fi.jakojaannos.konna.engine.view.UiRenderer;
import fi.jakojaannos.konna.engine.view.ui.Color;
import fi.jakojaannos.konna.engine.view.ui.Colors;
import fi.jakojaannos.konna.engine.view.ui.UiElement;
import fi.jakojaannos.konna.engine.view.ui.UiUnit;

import static fi.jakojaannos.konna.engine.view.ui.UiUnit.zero;

public class UiRendererRecorder implements UiRenderer {
    private final Supplier<VkExtent2D> framebufferSizeSupplier;

    private PresentableState state;

    public void setWriteState(final PresentableState state) {
        this.state = state;
    }

    public UiRendererRecorder(final Supplier<VkExtent2D> framebufferSizeSupplier) {

        this.framebufferSizeSupplier = framebufferSizeSupplier;
    }

    @Override
    public void setValue(final String key, final Object value) {
        this.state.uiVariables().set(key, value);
    }

    @Override
    public void draw(final UiElement element) {
        draw(element, 0, -1, -1, 2, 2);
    }

    private void draw(
            final UiElement element,
            final int depth,
            final double parentX,
            final double parentY,
            final double parentW,
            final double parentH
    ) {
        final var framebufferExtent = this.framebufferSizeSupplier.get();
        final var framebufferWidth = framebufferExtent.width();
        final var framebufferHeight = framebufferExtent.height();

        // Here, we need to convert element bounds into actual coordinates. There are a few possible
        // cases which need to be handled separately:
        //  Valid:
        //   1. left  and right are set, width must be calculated
        //   2. left  and width are set, right must be calculated
        //   3. right and width are set, left must be calculated
        //   4. none are set, left/right defaults to `0`, width must be calculated
        //   5. only left is set, right defaults to `0`, width must be calculated
        //   6. only right is set, left defaults to `0`, width must be calculated
        //
        //  Invalid (but recoverable):
        //   7. all are set, width is ignored and replaced with calculated value
        //
        // This boils down to:
        //   A. In cases 1, 4, 5 and 6 `width` is calculated. Figure out left/right and proceed to
        //      calculating the width. The "figure out" part is straightforward, as the missing
        //      value is just substituted with zero. Case 4 is actually special case of A. where
        //      both left and right must be set to default values before calculating the width.
        //   B. In cases 2 and 3 the missing left/right must be calculated using width
        //   C. Case 7. is (invalid) special case of A. If all are set, just ignore the width. There
        //      are "smarter" ways of handling this, but for simplicity's sake, just ignore the
        //      width for now.
        //
        // ...and then just do the same thing for top/bottom/height

        final var horizontal = resolveSize(element, parentW, framebufferWidth, element.left(), element.right(), element.width());
        final var vertical = resolveSize(element, parentH, framebufferHeight, element.top(), element.bottom(), element.height());
        final var anchorX = element.anchorX().calculate(element, parentW, framebufferWidth);
        final var anchorY = element.anchorY().calculate(element, parentH, framebufferHeight);

        final var entry = this.state.quadEntries().get();
        entry.x = parentX + anchorX + horizontal.min;
        entry.y = parentY + anchorY + vertical.min;
        entry.w = horizontal.size;
        entry.h = vertical.size;

        entry.z = depth;
        entry.color = element.color();

        for (final var child : element.children()) {
            draw(child, depth + 1, entry.x, entry.y, entry.w, entry.h);
        }
    }

    private SizeInfo resolveSize(
            final UiElement element,
            final double parentSize,
            final int framebufferSize,
            @Nullable final UiUnit minOffset,
            @Nullable final UiUnit maxOffset,
            @Nullable final UiUnit sizeValue
    ) {
        final var bothMinAndMaxAreSet = minOffset != null && maxOffset != null;
        final var actualSizeValue = bothMinAndMaxAreSet
                ? null
                : sizeValue;

        final var sizeIsNotSet = actualSizeValue == null;
        final double min;
        final double max;
        final double size;
        if (sizeIsNotSet) {
            // Case A/C: figure out min/max and calculate the size
            if (minOffset != null) {
                min = minOffset.calculate(element, parentSize, framebufferSize);
            } else {
                min = zero().calculate(element, parentSize, framebufferSize);
            }

            if (maxOffset != null) {
                max = maxOffset.calculate(element, parentSize, framebufferSize);
            } else {
                max = zero().calculate(element, parentSize, framebufferSize);
            }

            size = parentSize - (max + min);
        } else {
            // Case B: use pre-defined size to calculate missing min or max
            size = actualSizeValue.calculate(element, parentSize, framebufferSize);
            if (minOffset != null) {
                min = minOffset.calculate(element, parentSize, framebufferSize);
                max = parentSize - (min + size);
            } else /* if (maxOffset != null) */ {
                assert maxOffset != null;

                max = maxOffset.calculate(element, parentSize, framebufferSize);
                min = parentSize - (max + size);
            }
        }

        return new SizeInfo(min, max, size);
    }

    public static class QuadEntry implements Presentable {
        public double x;
        public double y;
        public double w;
        public double h;

        public int z;
        public Color color;

        @Override
        public void reset() {
            this.x = 0;
            this.y = 0;
            this.w = 0;
            this.h = 0;

            this.z = 0;
            this.color = Colors.TRANSPARENT_BLACK;
        }
    }

    private static record SizeInfo(double min, double max, double size) {}
}
