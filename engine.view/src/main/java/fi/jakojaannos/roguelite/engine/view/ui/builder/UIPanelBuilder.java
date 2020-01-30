package fi.jakojaannos.roguelite.engine.view.ui.builder;

import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.panel.BorderSize;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.panel.PanelSprite;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public final class UIPanelBuilder extends UIElementBuilder<UIPanelBuilder> {
    public UIPanelBuilder(
            final UserInterface userInterface,
            final Entity entity,
            final String name,
            final Consumer<Component> componentConsumer
    ) {
        super(userInterface, entity, name, componentConsumer);
    }

    public UIPanelBuilder borderSize(final int value) {
        this.componentConsumer.accept(new BorderSize(value));
        return this;
    }

    public UIPanelBuilder sprite(final String sprite) {
        this.componentConsumer.accept(new PanelSprite(sprite));
        return this;
    }
}
