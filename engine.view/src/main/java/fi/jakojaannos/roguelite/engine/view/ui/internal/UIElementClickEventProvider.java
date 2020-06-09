package fi.jakojaannos.roguelite.engine.view.ui.internal;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public class UIElementClickEventProvider {
    public void tick(
            final Stream<UIElement> roots,
            final UserInterface.UIEventBus eventBus,
            final Mouse mouse,
            final TimeManager timeManager
    ) {
        roots.map(UIElementImpl.class::cast)
             .filter(UIElementImpl::isActive)
             .forEach(uiElement -> {
                 final var name = uiElement.getProperty(UIProperty.NAME).orElseThrow();
                 if (mouse.clicked) {
                     if (!uiElement.isPressed()) {
                         uiElement.click(timeManager.getCurrentGameTime());
                         eventBus.fire(new UIEvent(name, UIEvent.Type.CLICK));
                     }
                 } else if (uiElement.isPressed()) {
                     uiElement.release();
                 }

                 tick(uiElement.getChildren().stream(),
                      eventBus,
                      mouse,
                      timeManager);
             });
    }
}
