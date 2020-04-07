package fi.jakojaannos.roguelite.engine.view.ui.builder;

import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Component;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public class GenericUIElementBuilder extends UIElementBuilder<GenericUIElementBuilder> {
    public GenericUIElementBuilder(
            final UserInterface userInterface,
            final EntityHandle entity,
            final String name,
            final Consumer<Object> componentConsumer
    ) {
        super(userInterface, entity, name, componentConsumer);
    }
}
