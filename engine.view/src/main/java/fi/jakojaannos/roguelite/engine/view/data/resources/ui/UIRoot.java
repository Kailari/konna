package fi.jakojaannos.roguelite.engine.view.data.resources.ui;

import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.ElementBoundaries;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UIRoot implements Resource {
    private final Viewport viewport;
    private final ElementBoundaries boundaries = new ElementBoundaries();

    public ElementBoundaries getBoundaries() {
        this.boundaries.minX = 0;
        this.boundaries.minY = 0;
        this.boundaries.maxX = this.boundaries.width = this.viewport.getWidthInPixels();
        this.boundaries.maxY = this.boundaries.height = this.viewport.getHeightInPixels();
        return this.boundaries;
    }

    public int getFontSize() {
        return 12;
    }
}
