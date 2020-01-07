package fi.jakojaannos.roguelite.engine.view.ui.builder;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

import java.util.function.Consumer;

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
