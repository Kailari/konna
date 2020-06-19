package fi.jakojaannos.riista.vulkan.assets.ui;

import javax.annotation.Nullable;

import fi.jakojaannos.riista.view.ui.UiUnit;

public final class Bounds {
    @Nullable private UiUnit top;
    @Nullable private UiUnit bottom;
    @Nullable private UiUnit left;
    @Nullable private UiUnit right;

    @Nullable private UiUnit width;
    @Nullable private UiUnit height;

    @Nullable
    public UiUnit top() {
        return this.top;
    }

    void top(final UiUnit value) {
        this.top = value;
    }

    @Nullable
    UiUnit bottom() {
        return this.bottom;
    }

    void bottom(final UiUnit value) {
        this.bottom = value;
    }

    @Nullable
    UiUnit left() {
        return this.left;
    }

    void left(final UiUnit value) {
        this.left = value;
    }

    @Nullable
    UiUnit right() {
        return this.right;
    }

    void right(final UiUnit value) {
        this.right = value;
    }

    @Nullable
    UiUnit width() {
        return this.width;
    }

    void width(final UiUnit value) {
        this.width = value;
    }

    @Nullable
    UiUnit height() {
        return this.height;
    }

    void height(final UiUnit value) {
        this.height = value;
    }
}
