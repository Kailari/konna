package fi.jakojaannos.riista.vulkan.renderer.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import fi.jakojaannos.riista.data.events.UiEvent;
import fi.jakojaannos.riista.view.Presentable;
import fi.jakojaannos.riista.view.ui.*;
import fi.jakojaannos.riista.vulkan.application.PresentableState;
import fi.jakojaannos.riista.vulkan.application.UiVariables;

import static fi.jakojaannos.riista.view.ui.UiUnit.zero;

public class UiRendererRecorder implements UiRenderer {
    private PresentableState state;

    public void setWriteState(final PresentableState state) {
        this.state = state;
    }

    @Override
    public void setValue(final String key, final Object value) {
        this.state.uiVariables().set(key, value);
    }

    @Override
    public List<UiEvent> draw(final UiElement element) {
        final var events = new ArrayList<UiEvent>();
        draw(events, element, 1, -1, -1, 2, 2);

        return events;
    }

    @Override
    public Collection<UiEvent> draw(
            final UiElement element,
            final float x,
            final float y,
            final float w,
            final float h
    ) {
        final var events = new ArrayList<UiEvent>();
        draw(events, element, 1, x, y, w, h);

        return events;
    }

    private void draw(
            final List<UiEvent> events,
            final UiElement element,
            final int depth,
            final double parentX,
            final double parentY,
            final double parentW,
            final double parentH
    ) {
        final var framebufferWidth = this.state.framebufferWidth();
        final var framebufferHeight = this.state.framebufferHeight();

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

        final var x = parentX + anchorX + horizontal.min;
        final var y = parentY + anchorY + vertical.min;
        final var width = horizontal.size;
        final var height = vertical.size;

        final var mouseX = this.state.mousePosition().x;
        final var mouseY = this.state.mousePosition().y;
        final var isHorizontallyIn = mouseX >= x && mouseX <= x + width;
        final var isVerticallyIn = mouseY >= y && mouseY <= y + height;

        final var isHovering = isHorizontallyIn && isVerticallyIn;
        final var maybeHoverElement = element.hoverElement();

        final var entry = this.state.quadEntries().get();
        entry.z = depth;

        final UiText text;
        if (isHovering && maybeHoverElement.isPresent()) {
            final var hoverElement = maybeHoverElement.get();
            final var hoverHorizontal = resolveSize(hoverElement,
                                                    parentW,
                                                    framebufferWidth,
                                                    Optional.ofNullable(hoverElement.left())
                                                            .orElse(element.left()),
                                                    Optional.ofNullable(hoverElement.right())
                                                            .orElse(element.right()),
                                                    Optional.ofNullable(hoverElement.width())
                                                            .orElse(element.width()));
            final var hoverVertical = resolveSize(hoverElement,
                                                  parentH,
                                                  framebufferHeight,
                                                  Optional.ofNullable(hoverElement.top())
                                                          .orElse(element.top()),
                                                  Optional.ofNullable(hoverElement.bottom())
                                                          .orElse(element.bottom()),
                                                  Optional.ofNullable(hoverElement.height())
                                                          .orElse(element.height()));
            entry.x = parentX + anchorX + hoverHorizontal.min;
            entry.y = parentY + anchorY + hoverVertical.min;
            entry.w = hoverHorizontal.size;
            entry.h = hoverVertical.size;

            entry.color = Optional.ofNullable(hoverElement.color())
                                  .orElse(element.color());
            text = Optional.ofNullable(hoverElement.text())
                           .orElse(element.text());

            if (this.state.mouseClicked()) {
                events.add(new UiEvent(element.name(), UiEvent.Type.CLICK));
            }
        } else {
            entry.x = x;
            entry.y = y;
            entry.w = width;
            entry.h = height;

            entry.color = element.color();

            text = element.text();
        }

        if (text != null) {
            final var textEntry = this.state.textEntries().get();
            textEntry.format = text.format();
            textEntry.argKeys = text.args();
            textEntry.alignment = text.align();
            textEntry.verticalAlignment = text.verticalAlign();
            textEntry.size = text.size();
            textEntry.color = text.color();

            textEntry.quad = entry;
        }

        for (final var child : element.children()) {
            draw(events, child, depth + 1, entry.x, entry.y, entry.w, entry.h);
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
        final double size;
        if (sizeIsNotSet) {
            // Case A/C: figure out min/max and calculate the size
            if (minOffset != null) {
                min = minOffset.calculate(element, parentSize, framebufferSize);
            } else {
                min = zero().calculate(element, parentSize, framebufferSize);
            }

            final var max = maxOffset != null
                    ? maxOffset.calculate(element, parentSize, framebufferSize)
                    : zero().calculate(element, parentSize, framebufferSize);

            size = parentSize - (max + min);
        } else {
            // Case B: calculate min

            size = actualSizeValue.calculate(element, parentSize, framebufferSize);
            if (minOffset != null) {
                min = minOffset.calculate(element, parentSize, framebufferSize);
            } else if (maxOffset != null) {
                final var max = maxOffset.calculate(element, parentSize, framebufferSize);
                min = parentSize - (max + size);
            } else /* if maxOffset == null && minOffset == null */ {
                min = zero().calculate(element, parentSize, framebufferSize);
            }
        }

        return new SizeInfo(min, size);
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

    public static class TextEntry implements Presentable {
        private static final QuadEntry NULL_QUAD = new QuadEntry();

        static {
            NULL_QUAD.reset();
        }

        public String format;
        public String[] argKeys;
        public Color color;
        public Alignment alignment;
        public Alignment verticalAlignment;
        public int size;

        public QuadEntry quad;

        @Override
        public void reset() {
            this.format = "";
            this.argKeys = new String[0];
            this.color = Colors.WHITE;
            this.alignment = null;
            this.verticalAlignment = null;

            this.quad = NULL_QUAD;
        }

        public String compileString(final UiVariables variables) {
            if (this.argKeys == null) {
                return this.format == null ? "" : this.format;
            }

            final var args = new Object[this.argKeys.length];
            for (int i = 0; i < args.length; i++) {
                args[i] = variables.get(this.argKeys[i]);
            }

            return String.format(this.format, args);
        }
    }

    private static record SizeInfo(double min, double size) {}
}
