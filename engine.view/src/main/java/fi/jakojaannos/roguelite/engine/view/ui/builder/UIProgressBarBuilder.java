package fi.jakojaannos.roguelite.engine.view.ui.builder;

import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public class UIProgressBarBuilder extends UIElementBuilder<UIProgressBarBuilder> {
    public UIProgressBarBuilder(
            final UserInterface userInterface,
            final EntityHandle entity,
            final String name,
            final Consumer<Object> componentConsumer
    ) {
        super(userInterface, entity, name, componentConsumer);
    }
}
