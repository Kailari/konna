package fi.jakojaannos.roguelite.engine.view.ui.builder;

import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Component;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public class UIProgressBarBuilder extends UIElementBuilder<UIProgressBarBuilder> {
    public UIProgressBarBuilder(
            final UserInterface userInterface,
            final Entity entity,
            final String name,
            final Consumer<Component> componentConsumer
    ) {
        super(userInterface, entity, name, componentConsumer);
    }
}
