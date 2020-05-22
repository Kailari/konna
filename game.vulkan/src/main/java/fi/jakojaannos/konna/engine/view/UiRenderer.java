package fi.jakojaannos.konna.engine.view;

import fi.jakojaannos.konna.engine.view.ui.UiElement;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;

public interface UiRenderer {
    /**
     * Gets an {@link UiElement UI element} for the given entity. Each entity has an associated ui element, which may be
     * used to position ui components to world-space, relative to their respective entities.
     * <p>
     * Additionally, entities with AABBs have their boundaries flattened to screen-space, so that their associated
     * element has size matching the screen-space boundaries of the entity.
     *
     * @param handle entity to get the element for
     *
     * @return the associated UI element for the entity
     */
    UiElement getElementForEntity(EntityHandle handle);
}
