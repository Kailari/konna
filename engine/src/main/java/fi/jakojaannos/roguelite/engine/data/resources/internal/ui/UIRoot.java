package fi.jakojaannos.roguelite.engine.data.resources.internal.ui;

import fi.jakojaannos.roguelite.engine.data.components.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UIRoot implements Resource {
    private final UserInterface.ViewportSizeProvider viewportSizeProvider;
    private final ElementBoundaries boundaries = new ElementBoundaries();

    public ElementBoundaries getBoundaries() {
        this.boundaries.minX = 0;
        this.boundaries.minY = 0;
        this.boundaries.maxX = this.boundaries.width = this.viewportSizeProvider.getWidthInPixels();
        this.boundaries.maxY = this.boundaries.height = this.viewportSizeProvider.getHeightInPixels();
        return this.boundaries;
    }

    public int getFontSize() {
        return 12;
    }
}
