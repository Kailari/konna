package fi.jakojaannos.konna.engine.view.ui.impl;

import javax.annotation.Nullable;

import fi.jakojaannos.konna.engine.view.ui.UiUnit;

import static fi.jakojaannos.konna.engine.view.ui.UiUnit.zero;

public final class Bounds {
    private UiUnit top = zero();
    private UiUnit bottom = zero();
    private UiUnit left = zero();
    private UiUnit right = zero();

    @Nullable private UiUnit width;
    @Nullable private UiUnit height;

    public UiUnit top() {
        return this.top;
    }

    void top(final UiUnit value) {
        this.top = value;
    }

    UiUnit bottom() {
        return this.bottom;
    }

    void bottom(final UiUnit value) {
        this.bottom = value;
    }

    UiUnit left() {
        return this.left;
    }

    void left(final UiUnit value) {
        this.left = value;
    }

    UiUnit right() {
        return this.right;
    }

    void right(final UiUnit value) {
        this.right = value;
    }

    UiUnit width() {
        // FIXME: Calculate from left+right+parent if null
        return this.width;
    }

    void width(final UiUnit value) {
        // FIXME: Log error if left+right are set
        this.width = value;
    }

    UiUnit height() {
        // FIXME: Calculate from left+right+parent if null
        return this.height;
    }

    void height(final UiUnit value) {
        // FIXME: Log error if top+bottom are set
        this.height = value;
    }
}
