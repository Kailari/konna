package fi.jakojaannos.roguelite.engine.data.components.ui;

import fi.jakojaannos.roguelite.engine.ecs.Component;

/**
 * Cached read-only view of a component's boundaries.
 * <p>
 * Note that while the component is not immutable, modifying these fields does nothing (but totally
 * messes up the rest of the UI rendering tick). To actually modify the ui element, use one of the
 * <code>BoundXXX</code> components.
 */
public class ElementBoundaries implements Component {
    public static int INVALID_VALUE = Integer.MIN_VALUE;

    public int minX, maxX;
    public int minY, maxY;
    public int width, height;
}
