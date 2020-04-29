package fi.jakojaannos.roguelite.engine.view.systems.ui;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.ui.internal.UIElementImpl;

public class UIElementHoverEventProvider {
    public void tick(
            final Stream<UIElement> roots,
            final UserInterface.UIEventBus eventBus,
            final Mouse mouse
    ) {
        roots.map(UIElementImpl.class::cast)
             .forEach(uiElement -> {
                 final var name = uiElement.getProperty(UIProperty.NAME).orElseThrow();

                 final var bounds = uiElement.getBounds();
                 if (isInside(mouse, bounds)) {
                     if (!uiElement.isActive()) {
                         uiElement.setActive(true);
                         eventBus.fire(new UIEvent(name, UIEvent.Type.START_HOVER));
                     }
                 } else {
                     if (uiElement.isActive()) {
                         uiElement.setActive(false);
                         eventBus.fire(new UIEvent(name, UIEvent.Type.END_HOVER));
                     }
                 }

                 tick(uiElement.getChildren().stream(),
                      eventBus,
                      mouse);
             });
    }

    private boolean isInside(final Mouse mouse, final ElementBoundaries bounds) {
        return mouse.position.x > bounds.minX && mouse.position.x < bounds.maxX
               && mouse.position.y > bounds.minY && mouse.position.y < bounds.maxY;
    }
}
