package fi.jakojaannos.roguelite.engine.view.ui.builder;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;

@SuppressWarnings("unchecked")
public abstract class UIElementBuilder<T extends UIElement, TBuilder extends UIElementBuilder<T, TBuilder>> {
    private boolean useOffsets = false;
    private boolean useSizeAndPos = false;

    public abstract T build();

    public TBuilder percentAnchor(final double percentX, final double percentY) {
        this.useSizeAndPos = true;
        return (TBuilder) this;
    }

    public TBuilder absoluteAnchor(final int x, final int y) {
        this.useSizeAndPos = true;
        return (TBuilder) this;
    }

    public TBuilder percentAnchor(
            final double left,
            final double right,
            final double top,
            final double bottom
    ) {
        this.useOffsets = true;
        return (TBuilder) this;
    }

    public TBuilder absoluteAnchor(
            final int left,
            final int right,
            final int top,
            final int bottom
    ) {
        this.useOffsets = true;
        return (TBuilder) this;
    }

    public TBuilder origin(final double percentX, final double percentY) {
        return (TBuilder) this;
    }

    public TBuilder size(final int width, final int height) {
        return (TBuilder) this;
    }
}
