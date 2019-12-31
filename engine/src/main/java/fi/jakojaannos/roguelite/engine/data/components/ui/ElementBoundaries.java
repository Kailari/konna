package fi.jakojaannos.roguelite.engine.data.components.ui;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.Getter;
import lombok.Setter;

/**
 * Cached read-only view of a component's boundaries.
 * <p>
 * Note that while the component is not immutable, modifying these fields does nothing (but totally
 * messes up the rest of the UI rendering tick). To actually modify the ui element, use one of the
 * <code>BoundXXX</code> components.
 */
public class ElementBoundaries implements Component {
    public static int INVALID_VALUE = Integer.MIN_VALUE;

    @Getter @Setter public int minX, maxX;
    @Getter @Setter public int minY, maxY;
    @Getter @Setter public int width, height;

    public void invalidate() {
        this.minX = ElementBoundaries.INVALID_VALUE;
        this.maxX = ElementBoundaries.INVALID_VALUE;
        this.minY = ElementBoundaries.INVALID_VALUE;
        this.maxY = ElementBoundaries.INVALID_VALUE;
        this.width = ElementBoundaries.INVALID_VALUE;
        this.height = ElementBoundaries.INVALID_VALUE;
    }
}
