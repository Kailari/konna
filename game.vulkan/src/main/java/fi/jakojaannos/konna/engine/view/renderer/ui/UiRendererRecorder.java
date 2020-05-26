package fi.jakojaannos.konna.engine.view.renderer.ui;

import org.lwjgl.vulkan.VkExtent2D;

import java.util.function.Supplier;

import fi.jakojaannos.konna.engine.application.PresentableState;
import fi.jakojaannos.konna.engine.view.Presentable;
import fi.jakojaannos.konna.engine.view.UiRenderer;
import fi.jakojaannos.konna.engine.view.ui.Color;
import fi.jakojaannos.konna.engine.view.ui.Colors;
import fi.jakojaannos.konna.engine.view.ui.UiElement;

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

        final var entry = this.state.quadEntries().get();
        final var left = element.left().calculate(element, parentW, framebufferWidth);
        final var right = element.right().calculate(element, parentW, framebufferWidth);
        final var top = element.top().calculate(element, parentH, framebufferHeight);
        final var bottom = element.bottom().calculate(element, parentH, framebufferHeight);

        final var anchorX = element.anchorX().calculate(element, parentW, framebufferWidth);
        final var anchorY = element.anchorY().calculate(element, parentH, framebufferHeight);

        entry.x = parentX + anchorX + left;
        entry.y = parentY + anchorY + top;

        final var rightCoordinate = parentX + parentW - right;
        final var bottomCoordinate = parentY + parentH - bottom;
        entry.w = rightCoordinate - entry.x;
        entry.h = bottomCoordinate - entry.y;

        entry.z = depth;
        entry.color = element.color();

        for (final var child : element.children()) {
            draw(child, depth + 1, entry.x, entry.y, entry.w, entry.h);
        }
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
}
