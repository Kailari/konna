package fi.jakojaannos.konna.engine.view.renderer.ui;

import fi.jakojaannos.konna.engine.application.PresentableState;
import fi.jakojaannos.konna.engine.view.Presentable;
import fi.jakojaannos.konna.engine.view.UiRenderer;
import fi.jakojaannos.konna.engine.view.ui.UiElement;

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
    public void draw(final UiElement element) {
        draw(element, 0, 0, 0, 0, 0);
    }

    private void draw(
            final UiElement element,
            final int depth,
            final double parentX,
            final double parentY,
            final double parentW,
            final double parentH
    ) {
        final var entry = this.state.quadEntries().get();
        final var left = element.left().calculate(element, parentW);
        final var right = element.right().calculate(element, parentW);
        final var top = element.top().calculate(element, parentH);
        final var bottom = element.bottom().calculate(element, parentH);

        entry.x = parentX + left;
        entry.y = parentY + top;
        entry.w = right - left;
        entry.h = bottom - top; // FIXME: Is this correct?

        entry.z = depth;

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

        @Override
        public void reset() {
            this.x = 0;
            this.y = 0;
            this.w = 0;
            this.h = 0;

            this.z = 0;
        }
    }
}
